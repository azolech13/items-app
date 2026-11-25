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


