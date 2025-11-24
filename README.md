# README

## Problemformulering

Formålet med projektet er at udvikle en full-stack webapplikation, der simulerer et ruteplanlægnings- og leveringssystem i en fiktiv by.  
Systemet modellerer en by som et grid med veje, bygninger, parker, flod og broer. Der er faste restauranter og bude (couriers), som får tildelt disse ordrer.

Kernen i projektet er:

- at beregne ruter ved hjælp af A* (og en naiv variant via heuristikken NONE),
- at understøtte multi-stop ruter med forskellige leveringsstrategier (IN_ORDER og NEAREST_NEIGHBOR),
- at simulere, hvordan valg af algoritme og strategi påvirker den samlede distance, antal besøgte noder og fordeling af ordrer mellem bude.


---

## Installation og kørsel

### Forudsætninger

- Java (JDK 17 eller nyere)
- Maven
- Node.js og npm

Projektet er opdelt i to dele: backend (Spring Boot) og frontend (Vite/JavaScript).

### Backend

Clone git repositoryet:

https://github.com/MarcusKM95/routeplanner-backend-new.git

Åben terminalen i projektet og kør:

mvn spring-boot:run

Dette starter backend-serveren på http://localhost:8080.

### Frontend
Clone git repositoryet:
https://github.com/MarcusKM95/route-planner-frontend.git

Åben terminalen i frontend-projektet og kør:
npm install

Derefter: 
npm run dev
Dette starter frontend-serveren på http://localhost:5173.
---
## Vejledning til brug af applikationen

vejledning i brug

Når backend og frontend kører:

Åbn frontendens URL i din browser (fx http://localhost:5173).

Øverste kort (ruteplanlægning):

Vælg en restaurant i dropdown.

Klik på kortet for at tilføje et eller flere stops.

Vælg heuristik (MANHATTAN, EUCLIDEAN, NONE) og strategi (IN_ORDER, NEAREST_NEIGHBOR).

Tryk på knappen for at beregne ruten.

Se distance, antal besøgte noder og ruten tegnet på kortet.

Nederste kort (ordrer og simulation):

Vælg en restaurant for nye ordrer.

Klik på nederste kort for at oprette en ordre til den valgte restaurant.

Systemet opretter ordren og tildeler automatisk en kurér.

Bude bevæger sig i små skridt på kortet, afhenter ordrer ved restauranterne og leverer dem til kunderne.

Aktive og leverede ordrer vises i paneler under kortet.

---

## Brug af AI 

Jeg har under projektet brugt ChatGPT til at få hjælp til specifikke kodningsproblemer og koncepter, især omkring implementering af A*-algoritmen og håndtering af datastrukturer i Java. AI'en har hjulpet med at forklare komplekse emner og foreslå kodeeksempler, som jeg derefter har tilpasset og integreret i mit projekt. Jeg har så vidt muligt selv kode, og bedt den om at holde lidt igen med bare at dumpe copy-paste kode. Jeg har ved større bugs copy-pasted for at se om det løste problemerne, hvis jeg har været helt på bar bund. Løste det problemet, har jeg dog sat mig ind i hvorfor det løste problemet.

Tests er genereret af AI. 
