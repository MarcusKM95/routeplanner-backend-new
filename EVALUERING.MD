EVALUERING.md

Denne evaluering beskriver de valg, jeg har truffet omkring rutealgoritmer, datastrukturer og strategier i projektet. Projektet består af et ruteplanlægnings- og leveringssystem, der simulerer en by med veje, bygninger, parker, vægte, broer og en flod. Couriers modtager ordrer og kører ruter beregnet med A* eller med en naiv variant (Dijkstra). Derudover anvendes to strategier til at sortere leveringsstop: IN_ORDER og NEAREST_NEIGHBOR.

Jeg har undersøgt forskellene ved mindre simulerede scenarier. Formålet har været at vurdere hvordan de forskellige valg påvirker systemets adfærd.

Evaluering af A* (MANHATTAN) vs. Dijkstra (NONE)

Rutealgoritmen er implementeret som A*, hvor heuristikken kan vælges frit i frontend. Ved at vælge heuristikken NONE får man i praksis Dijkstra, fordi heuristikken bliver nul og A* derfor opfører sig som en ren best-first søgning på G-værdier.

Begge algoritmer finder den korteste vej, men de gør det på forskellig måde. A* prioriterer felter, der “peger mod” målet, mens Dijkstra undersøger flere felter rundt om startpunktet uden retning. Det betyder, at begge metoder typisk ender med samme distance, men forskelligt antal besøgte noder og forskellig køretid.

Jeg har testet to små scenarier:

1 restaurant og 1 ordre
A* (MANHATTAN): distance = (14.6), besøgte noder = (70)

Dijkstra (NONE): distance =(14,6), besøgte noder =(245)

1 restaurant og 3 ordrer
Her var forskellen tydeligere, fordi ruten består af flere legs.
A*: (44) besøgte noder: (103)

Dijkstra: (44) besøgte noder: (685)



I begge scenarier fandt A* samme eller næsten samme distance som Dijkstra, men A* brugte betydeligt færre noder. Det så ud til, at Dijkstra altid laver en større søgning, hvor den udfolder store cirkler rundt om startpunktet, mens A* ret hurtigt bevæger sig mod målet. Derfor er A* klart hurtigst i dette domæne, selv med et relativt lille grid.

Evaluering af leveringsstrategier: IN_ORDER vs. NEAREST_NEIGHBOR

Til planlægning af flere leveringsstop er der to strategier:

IN_ORDER: stoppene tages i rækkefølgen de blev tilføjet
NEAREST_NEIGHBOR: kureren tager altid det nærmeste næste stop

Jeg testede dem i samme situation og så på besparelsen/differencen.

Tre leveringer fra samme restaurant.

IN_ORDER kørte dem i indtastet rækkefølge. NEAREST_NEIGHBOR valgte først den kunde, der lå tættest på restauranten, og derefter de andre.

Resultater:

IN_ORDER total distance = (61.8)
NEAREST_NEIGHBOR total distance = (35.6)

Min vurdering er, at NEAREST_NEIGHBOR generelt gav ruter der var kortere og mere naturlige i bykortet, fordi strategien ikke ignorerer de rumlige forhold. IN_ORDER fungerer mest som kontrolgruppe.



Overordnet vurdering

A* viste sig at være den mest effektive algoritme i alle scenarier, især når byen indeholder vægte og forhindringer som floden, broer og bygninger. Dijkstra fandt altid samme vej, men på bekostning af langt flere besøgte noder. Leveringsstrategierne gav helt forskellige ruter, hvor NEAREST_NEIGHBOR virkede mest realistisk og resulterede i kortere kørsel. 

Samlet set fungerer kombinationen af A* + NEAREST_NEIGHBOR bedst i den bymodel jeg har designet, og simuleringerne viser tydelig forskel i adfærd og ydelse mellem de forskellige valgmuligheder.