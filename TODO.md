1. korriger feil i veteranpoeng
   - Problemet er at melter faber legges til hver øvelse og avrundes, men skal ganges til slutt, så avrundes...
2. Korriger "det som trengs" dersom rykk eller støt er gjeldene øvlse, da avrunding
   kan bli feil (avrunder før legger sammen) (MERK: at til slutt vil total bli korrekt. Dette er håndtert.)

3. Bruk enum for øvelser isteden for streng sammenlikning! "rykk", etc

4. Lag tester, spessielt for poengberegning

5. Lag en klasse for hver øvelse for bedre kode, lag evt en wrapper for rykk
   og støt, slik at total, poeng etc blir mer logisk... (samt færre looper rundt om)

6. Lag readme med bedre dokumentasjon
