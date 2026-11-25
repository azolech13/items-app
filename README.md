# items-app

Backend do zarządzania notatkami. Java 21, Spring Boot 3.5, MySQL 8.4, Liquibase i Hibernate Envers.

## Architektura

Tabele biznesowe należą do Liquibase. Changelog jest w `src/main/resources/db/changelog/db.changelog-master.yaml` i włącza po kolei:

- `db/changeset/001-create-users.yml`
- `db/changeset/002-create-items.yml`
- `db/changeset/003-create-item-permissions.yml`

`spring.jpa.hibernate.ddl-auto` zostaje na `update`, bo tabele historii ma założyć Hibernate Envers, gdy pojawi się encja z `@Audited` i własna `RevisionEntity`. `items_aud` nie ma w changelogu: wynika to z założeń zadania, które każe zostawić tę tabelę Enversowi.

Żeby `update` nie zmieniał tabel prowadzonych w Liquibase, `LiquibaseTablesFilterProvider` wyłącza je ze schematu Hibernate. Klasa jest publiczna, bo Hibernate tworzy ją po nazwie z konfiguracji. Leży w `infrastructure.adapter.out.persistence`.

Nowa tabela zarządzana przez Liquibase wymaga dwóch wpisów:

1. plik changesetu włączony w `db.changelog-master.yaml`
2. nazwa tabeli w zbiorze `OWNED_BY_LIQUIBASE` w `LiquibaseTablesFilterProvider`

Bez drugiego wpisu `ddl-auto: update` zacznie tę tabelę modyfikować. Jedynym źródłem zmian schematu tabel biznesowych jest Liquibase. Filtr nie zgłasza rozjazdu encji z changelogiem, więc kolumny w changesecie i w encji mają się zgadzać.

Hasło bazy w `application.yml` jest deweloperskie, na potrzeby zadania. W produkcji nie powinno leżeć w repozytorium: podstawia się je przy starcie, na przykład z vaulta.

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

## API

`POST /register` jest publiczny. Body to JSON z polami `login` (3–64 znaki) i `password` (minimum 8 znaków). Hasło w bazie leży jako hash bcrypt. Odpowiedź `201` zawiera `id`, `login` i `createdAt`. Zajęty login zwraca `409`, niepoprawne pola `400`.

`POST /login` jest publiczny. Body to `login` i `password`. Odpowiedź `200` zawiera `token` i `expiresIn`. Brak pola to `400`, złe dane to `401`. Z jednego adresu IP można wysłać 5 żądań na minutę. Szóste zwraca `429` i nagłówek `Retry-After`. Limit i czas życia tokena są w `application.yml`. Token jest podpisany HMAC, sekret nie trafia do klienta.

`POST /items` wymaga nagłówka `Authorization: Bearer`. Body to `title` (1–255 znaków) i opcjonalny `content`. Właścicielem zostaje użytkownik z tokena. Odpowiedź `201` zawiera `id`, `title`, `content`, `version`, `ownerId`, `createdAt` i `updatedAt`. Nowa notatka ma `version` równe 0. Brak albo złe pole to `400`, brak albo zły token to `401`. Zapis dopisuje rewizję Enversa.

`GET /items` wymaga tego samego nagłówka. Zwraca `200` i listę notatek użytkownika: własne z `myRole` równym `OWNER` oraz udostępnione z rolą `EDITOR` albo `VIEWER`. Usunięte notatki nie wchodzą. Element ma `id`, `title`, `content`, `version`, `ownerId`, `myRole` i `updatedAt`. Pusta lista to `[]`. Brak albo zły token to `401`.

`PATCH /items/{id}` wymaga tego samego nagłówka. Body ma wymagane `version`. Brak `title` zostawia dotychczasowy tytuł. `content` zapisuje się tak, jak przyszedł, także jako `null`, gdy pola nie ma. Edytować może właściciel i użytkownik z rolą `EDITOR`. Odpowiedź `200` zawiera `id`, `title`, `content`, `version` i `updatedAt`. Brak `version` albo złe pole to `400`, brak albo zły token to `401`, `VIEWER` albo obcy użytkownik to `403`, brak albo usunięta notatka to `404`. Niezgodna wersja to `409`, a w treści jest aktualna `version`.

`DELETE /items/{id}` wymaga tego samego nagłówka. Usuwa notatkę miękko: `deleted` staje się `true` i notatka znika z listy. Może to zrobić tylko właściciel. Odpowiedź `204` nie ma treści. Brak albo zły token to `401`, inny użytkownik to `403`, brak albo już usunięta notatka to `404`.

`GET /items/{id}/history` wymaga tego samego nagłówka. Zwraca `200` i rewizje Enversa od najstarszej. Element ma `revision`, `revisionType` (`ADD`, `MOD`, `DEL`), `timestamp`, `changedBy` (login autora zmiany), `title` i `content`. Miękkie usunięcie jest rewizją `DEL`. Czytać może właściciel oraz użytkownik z rolą `VIEWER` albo `EDITOR`, także gdy notatka jest już usunięta. Pusta lista to `[]`. Brak albo zły token to `401`, brak dostępu to `403`, brak notatki to `404`. 

Przykładowe requesty są w `example-requests.http`. Da się je uruchomić lokalnie na `http://localhost:8080`, gdy aplikacja już działa: cały Compose albo profil `local`. Token z logowania wstawia się w żądanie tworzenia notatki.


