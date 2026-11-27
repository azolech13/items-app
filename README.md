# items-app

Backend do zarządzania notatkami. Java 21, Spring Boot 3.5, MySQL 8.4, Liquibase i Hibernate Envers.

## Architektura

### Warstwy i architektura heksagonalna

Aplikacja jest podzielona zgodnie z architekturą heksagonalną (porty i adaptery):

- `domain` — niemutowalne rekordy (`User`, `Item`, `ItemPermission`), enumy ról i typów rewizji oraz hierarchie wyjątków (`sealed class` + `permits`), całkowicie bez zależności od Springa i JPA,
- `application` — porty wejściowe (`port.in`), wyjściowe (`port.out`) oraz serwisy realizujące przypadki użycia (`service`),
- `infrastructure` — adaptery wejściowe (`adapter.in.web`, `adapter.in.security`) oraz wyjściowe (`adapter.out.persistence`, `adapter.out.security`, `adapter.out.time`, `adapter.out.uuid`).

Takie rozdzielenie zwiększa czytelność, wymusza jednoznaczny kierunek zależności do środka aplikacji, ułatwia testowanie logiki biznesowej na mockach portów (bez kontekstu Springa i bazy) oraz pozwala wymieniać adaptery techniczne bez modyfikacji logiki domenowej.

**Świadome uproszczenie:** warstwa `application.service` korzysta bezpośrednio z adnotacji Springa (`@Service`, `@Component`, `@Transactional`). W czystym wariancie heksagonalnym serwisy powinny pozostać czystą Javą, a rejestracja beanów i granice transakcji powinny trafić do konfiguracji w warstwie infrastruktury. Na potrzeby zadania przyjęto lżejszy wariant bez dodatkowego kodu konfiguracyjnego.

### Bezpieczeństwo i autoryzacja

- **JWT z podpisem HMAC zamiast pary kluczy (RSA/ECDSA):** jedna aplikacja wystawia token i sama go weryfikuje, więc osobny klucz publiczny niczego tu nie izoluje. Podpis asymetryczny ma sens w architekturze rozproszonej, gdy jedna usługa podpisuje tokeny, a pozostałe dostają wyłącznie klucz publiczny do ich sprawdzania. Klucz z `application.yml` jest sekretem deweloperskim zakodowanym w Base64 i dekodowanym przez `Decoders.BASE64.decode(...)` — w produkcji taki klucz powinien trafiać z vaulta lub zmiennych środowiskowych.
- **Wyłączony CSRF (`STATELESS` + SPA):** aplikacja działa bez sesji HTTP, a uwierzytelnianie odbywa się przez nagłówek `Authorization: Bearer <token>` (np. z aplikacji SPA w Angularze). Ponieważ przeglądarka nie dołącza tego nagłówka automatycznie do żądań między domenami, klasyczny atak CSRF nie dotyczy tego modelu.
- **XSS a przechowywanie tokena po stronie klienta:** trzymanie JWT w `localStorage` eliminuje CSRF, ale naraża token na odczyt w razie podatności XSS na froncie. Dla JSON REST API Spring Security dodaje domyślne nagłówki bezpieczeństwa (`X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Cache-Control: no-store`), natomiast kluczowa ochrona przed XSS leży po stronie aplikacji SPA (wbudowana sanityzacja Angulara i nagłówek `Content-Security-Policy` serwowany razem z frontendem). W systemie produkcyjnym bezpieczniejszą alternatywą dla `localStorage` jest krótko żyjący access token trzymany w pamięci i refresh token w ciasteczku `HttpOnly; Secure; SameSite` (albo wzorzec BFF).
- **Autoryzacja na poziomie zasobu:** token zawiera wyłącznie identyfikator użytkownika (`sub`), bez ról i listy notatek. Uprawnienia (`OWNER` z `items.owner_id`, `EDITOR`/`VIEWER` z `item_permissions`) są sprawdzane przy każdym żądaniu w warstwie aplikacji, dzięki czemu zmiana lub cofnięcie dostępu działa natychmiast, bez czekania na wygaśnięcie JWT.

### Baza danych, historia zmian i współbieżność

Tabele biznesowe należą do Liquibase. Changelog jest w `src/main/resources/db/changelog/db.changelog-master.yaml` i włącza po kolei:

- `db/changeset/001-create-users.yml`
- `db/changeset/002-create-items.yml`
- `db/changeset/003-create-item-permissions.yml`
- `db/changeset/004-add-item-permission-timestamps.yml`

`spring.jpa.hibernate.ddl-auto` zostaje na `update`, bo tabele historii ma założyć Hibernate Envers na podstawie `@Audited` w `ItemEntity` oraz własnej encji `AuthorRevision` (`@RevisionEntity`). `items_aud` nie ma w changelogu: wynika to z założeń zadania, które każe zostawić tę tabelę Enversowi.

Żeby `update` nie zmieniał tabel prowadzonych w Liquibase, `LiquibaseTablesFilterProvider` wyłącza je ze schematu Hibernate. Klasa jest publiczna, bo Hibernate tworzy ją po nazwie z konfiguracji. Leży w `infrastructure.adapter.out.persistence`.

Nowa tabela zarządzana przez Liquibase wymaga dwóch wpisów:

1. plik changesetu włączony w `db.changelog-master.yaml`
2. nazwa tabeli w zbiorze `OWNED_BY_LIQUIBASE` w `LiquibaseTablesFilterProvider`

Bez drugiego wpisu `ddl-auto: update` zacznie tę tabelę modyfikować. Jedynym źródłem zmian schematu tabel biznesowych jest Liquibase. Filtr nie zgłasza rozjazdu encji z changelogiem, więc kolumny w changesecie i w encji mają się zgadzać.

- **Generowanie UUID i czasu w aplikacji (`UuidGenerator`, `DateTimeProvider`):** identyfikatory UUID są nadawane przed zapisem w bazie, bez `@GeneratedValue` po stronie JPA. Dzięki temu obiekt ma stałe, niezmienne `id` już od momentu utworzenia (bez stanu z `id == null` przed `persist`/`flush`), co pozwala bezpiecznie oprzeć ewentualne `equals`/`hashCode` encji wyłącznie o samo `id` zamiast modyfikowalnych pól i używać jej w kolekcjach haszujących bez ryzyka „zgubienia” elementu po zapisie. Zarówno `UUID.randomUUID()`, jak i `Instant.now()` zostały zamknięte za portami wyjściowymi (`UuidGenerator`, `DateTimeProvider`), żeby łatwo je mockować w deterministycznych testach jednostkowych.
- **Optimistic locking:** encja `ItemEntity` ma `@Version`. Przy `PATCH` wersja jest sprawdzana w serwisie oraz egzekwowana przez Hibernate podczas `saveAndFlush`, żeby odpowiedź `200` zawierała już podbitą wartość `version`, a konflikt zwracał `409` z aktualną wersją w komunikacie.
- **Soft delete:** usunięcie notatki ustawia `deleted = true`, dzięki czemu domyślne zapytania ją pomijają, a historia w Enversie pozostaje dostępna (zmiana flagi `deleted` na `true` jest mapowana w odpowiedzi historii na `DEL`).
- **Sekrety deweloperskie:** hasło bazy i klucz JWT w `application.yml` służą wyłącznie do uruchomienia zadania lokalnie i w Docker Compose. W produkcji nie leżą w repozytorium, tylko są podstawiane przy starcie (np. z vaulta).

### Rate limiting

Endpoint `POST /login` chroni filtr `LoginRateLimitFilter` oparty o Bucket4j z kubełkami trzymanymi w pamięci per adres IP (bez zewnętrznych zależności takich jak Redis, zgodnie z wymaganiem zadania). W środowisku wieloinstancyjnym limitowanie należałoby przenieść do współdzielonego magazynu (np. Redis) albo na warstwę API Gateway / reverse proxy.

## Uruchomienie

Potrzebny jest Docker. 

### Całość w Docker Compose

Z katalogu projektu:

```bash
docker compose up --build
```

Compose stawia MySQL 8.4 i aplikację. Aplikacja dostaje profil `docker` i czeka, aż baza przejdzie healthcheck. Aplikacja słucha na `http://localhost:8080`, MySQL na `localhost:3306`.

Zatrzymanie:

```bash
docker compose down
```

Dane MySQL zostają w wolumenie `mysql-data`. `docker compose down -v` usuwa też wolumen.

### Sama baza i aplikacja z IDE

```bash
docker compose up mysql
```

W konfiguracji uruchomienia ustaw profil `local`. Adres bazy jest wtedy `jdbc:mysql://localhost:3306/items`.

Z linii poleceń, przy zainstalowanym JDK 21:

```bash
# Windows
gradlew.bat bootRun --args="--spring.profiles.active=local"

# Linux i macOS
./gradlew bootRun --args='--spring.profiles.active=local'
```

Bez aktywnego profilu aplikacja nie ma adresu JDBC. `local` wskazuje `localhost`, `docker` wskazuje host `mysql` w sieci Compose.

### Testy integracyjne

Potrzebny jest działający Docker.

```bash
gradlew.bat integrationTest
```

Zadanie buduje JAR, stawia MySQL 8.4 i uruchamia ten JAR w kontenerze. Sprawdza historię Enversa przez `GET /items/{id}/history` oraz limit logowania: szóste `POST /login` z jednego adresu zwraca `429` i nagłówek `Retry-After`. `gradlew.bat test` zostawia te testy i odpala tylko jednostkowe.

## API

`POST /register` jest publiczny. Body to JSON z polami `login` (3–64 znaki) i `password` (minimum 8 znaków). Hasło w bazie leży jako hash bcrypt. Odpowiedź `201` zawiera `id`, `login` i `createdAt`. Zajęty login zwraca `409`, niepoprawne pola `400`.

`POST /login` jest publiczny. Body to `login` i `password`. Odpowiedź `200` zawiera `token` i `expiresIn`. Brak pola to `400`, złe dane to `401`. Z jednego adresu IP można wysłać 5 żądań na minutę. Szóste zwraca `429` i nagłówek `Retry-After`. Limit i czas życia tokena są w `application.yml`. Token jest podpisany HMAC, sekret nie trafia do klienta.

`POST /items` wymaga nagłówka `Authorization: Bearer`. Body to `title` (1–255 znaków) i opcjonalny `content`. Właścicielem zostaje użytkownik z tokena. Odpowiedź `201` zawiera `id`, `title`, `content`, `version`, `ownerId`, `createdAt` i `updatedAt`. Nowa notatka ma `version` równe 0. Brak albo złe pole to `400`, brak albo zły token to `401`. Zapis dopisuje rewizję Enversa.

`GET /items` wymaga tego samego nagłówka. Zwraca `200` i listę notatek użytkownika: własne z `myRole` równym `OWNER` oraz udostępnione z rolą `EDITOR` albo `VIEWER`. Usunięte notatki nie wchodzą. Element ma `id`, `title`, `content`, `version`, `ownerId`, `myRole` i `updatedAt`. Pusta lista to `[]`. Brak albo zły token to `401`.

`PATCH /items/{id}` wymaga tego samego nagłówka. Body ma wymagane `version`. Brak `title` albo `title` równe `null` zostawia dotychczasowy tytuł. Brak `content` zostawia dotychczasową treść. `content` równe `null` czyści treść. Edytować może właściciel i użytkownik z rolą `EDITOR`. Odpowiedź `200` zawiera `id`, `title`, `content`, `version` i `updatedAt`. Brak `version` albo złe pole to `400`, brak albo zły token to `401`, `VIEWER` albo obcy użytkownik to `403`, brak albo usunięta notatka to `404`. Niezgodna wersja to `409`, a w treści jest aktualna `version`.

`DELETE /items/{id}` wymaga tego samego nagłówka. Usuwa notatkę miękko: `deleted` staje się `true` i notatka znika z listy. Może to zrobić tylko właściciel. Odpowiedź `204` nie ma treści. Brak albo zły token to `401`, inny użytkownik to `403`, brak albo już usunięta notatka to `404`.

`POST /items/{id}/share` wymaga tego samego nagłówka. Body to `userId` i `role` (`VIEWER` albo `EDITOR`). Nadawać dostęp może tylko właściciel. Nowa zgoda zwraca `201`, zmiana istniejącej roli `200`. Odpowiedź ma `itemId`, `userId`, `role` i `grantedAt`, a `grantedAt` jest czasem ostatniej zmiany zgody. Nadanie roli samemu sobie to `400`, zła rola albo brak pola też `400`, brak albo zły token to `401`, inny użytkownik to `403`, brak notatki albo brak użytkownika to `404`.

`DELETE /items/{id}/share/{userId}` wymaga tego samego nagłówka. Cofa dostęp wskazanemu użytkownikowi. Może to zrobić tylko właściciel. Odpowiedź `204` nie ma treści. Brak albo zły token to `401`, inny użytkownik to `403`, brak albo usunięta notatka oraz brak nadanego dostępu to `404`.

`GET /items/{id}/history` wymaga tego samego nagłówka. Zwraca `200` i rewizje Enversa od najstarszej. Element ma `revision`, `revisionType` (`ADD`, `MOD`, `DEL`), `timestamp`, `changedBy` (login autora zmiany), `title` i `content`. Miękkie usunięcie jest rewizją `DEL`. Czytać może właściciel oraz użytkownik z rolą `VIEWER` albo `EDITOR`, także gdy notatka jest już usunięta. Pusta lista to `[]`. Brak albo zły token to `401`, brak dostępu to `403`, brak notatki to `404`. 

Przykładowe requesty są w `example-requests.http`. Da się je uruchomić lokalnie na `http://localhost:8080`, gdy aplikacja już działa: cały Compose albo profil `local`. Token z logowania wstawia się w żądanie tworzenia notatki.

## Konfiguracja

Parametry aplikacji w `src/main/resources/application.yml` oraz profilach `application-local.yml` i `application-docker.yml`:

| Parametr | Wartość domyślna | Opis |
| --- | --- | --- |
| `spring.profiles.active` | brak (`local` lub `docker`) | Wybiera adres bazy: `localhost` dla uruchomienia lokalnego albo host `mysql` w sieci Docker Compose. |
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/items` (`local`), `jdbc:mysql://mysql:3306/items` (`docker`) | Adres JDBC bazy MySQL. |
| `spring.datasource.username` | `items` | Użytkownik bazy MySQL. |
| `spring.datasource.password` | `items` | Hasło deweloperskie do bazy MySQL (w produkcji podstawiane zewnętrznie, np. z vaulta). |
| `spring.jpa.hibernate.ddl-auto` | `update` | Pozwala Hibernate Envers utworzyć tabele audytowe; tabele Liquibase są wykluczone przez `LiquibaseTablesFilterProvider`. |
| `app.security.jwt.secret` | `aXRlbXMtYXBwLWRldi1qd3Qtc2VjcmV0LTMyYi1taW4=` | Klucz HMAC zakodowany w Base64 (min. 32 bajty po zdekodowaniu) do podpisywania i weryfikacji tokenów JWT. |
| `app.security.jwt.expiration-seconds` | `3600` | Czas ważności tokena JWT w sekundach. |
| `app.security.login-rate-limit.attempts` | `5` | Maksymalna liczba żądań `POST /login` na adres IP w jednym oknie czasu. |
| `app.security.login-rate-limit.window` | `1m` | Długość okna rate limitera Bucket4j dla `POST /login`. |



