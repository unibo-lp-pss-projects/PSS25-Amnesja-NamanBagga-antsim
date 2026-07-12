# PSS25-antsim: Simulazione Ecosistema di Formiche
## Email dei componenti del gruppo

- **Naman Bagga** → naman.bagga@studio.unibo.it
- **Salvatore Persico** → salvatore.persico4@studio.unibo.it

---

## Obiettivo del progetto

L'obiettivo del progetto è sviluppare un'applicazione Java che realizzi una simulazione di un ecosistema artificiale basato su una colonia di formiche, ispirata ai principi dei sistemi multi-agente e della Ant Colony Optimization (un algoritmo ispirato al comportamento reale delle formiche per trovare percorsi ottimali nella ricerca di cibo).

Il sistema simulerà un ambiente bidimensionale contenente una colonia di formiche, fonti di cibo e ostacoli. Le formiche agiranno come agenti autonomi in grado di esplorare l'ambiente, raccogliere risorse e comunicare indirettamente tramite il rilascio di feromoni.

La simulazione dovrà evidenziare comportamenti emergenti, come la formazione di percorsi efficienti tra il nido e le fonti di cibo.

Il risultato atteso è un'applicazione eseguibile dotata di una semplice interfaccia grafica che consenta di visualizzare l'evoluzione della simulazione nel tempo.

---

## Funzionalità necessarie

- Rappresentazione dell’ambiente come griglia bidimensionale
- Modellazione di:
    - nido
    - fonti di cibo
    - ostacoli

- Implementazione di agenti “formiche” con:
    - posizione nello spazio
    - stato (ricerca cibo / ritorno al nido)
    - movimento probabilistico

- Sistema di feromoni:
    - deposizione durante il trasporto del cibo
    - evaporazione nel tempo

- Simulazione a passi discreti (loop principale)
- Visualizzazione grafica dello stato della simulazione (tramite JavaFX)

---

## Funzionalità opzionali

- Introduzione di più tipi di feromoni (es. cibo e pericolo)
- Sistema energetico per le formiche (consumo e morte)
- Generazione dinamica delle risorse
- Parametri configurabili della simulazione
- Raccolta e visualizzazione di statistiche (es. quantità di cibo raccolto)
- Introduzione di agenti predatori

---

## Challenge previste

- Modellazione del comportamento emergente degli agenti
- Gestione efficiente della struttura dati della griglia
- Bilanciamento dei parametri della simulazione (movimento, evaporazione dei feromoni, ecc.)
- Separazione tra logica di dominio e interfaccia grafica secondo principi di progettazione object-oriented
- Integrazione e coordinamento delle diverse componenti del sistema

---

## Suddivisione del lavoro

Il lavoro sarà suddiviso per componenti principali del sistema, garantendo che entrambi contribuiscano sia alla logica applicativa sia alla componente di visualizzazione, così da coprire in modo equilibrato tutti gli aspetti del progetto.

---

### Salvatore Persico

**Modellazione del dominio principale:**
- definizione delle classi fondamentali (Ant ecc.)
- progettazione delle strutture dati per la rappresentazione dello stato della simulazione

**Implementazione del comportamento delle formiche:**
- movimento probabilistico
- gestione degli stati (ricerca del cibo / ritorno al nido)
- scelta delle direzioni in base ai livelli di feromone

**Implementazione del sistema di feromoni:**
- deposizione durante il trasporto del cibo
- evaporazione nel tempo
- gestione della diffusione locale dei feromoni

**Contributo alla componente grafica:**
- rendering dinamico delle formiche
- visualizzazione dell’intensità dei feromoni
- aggiornamento grafico coerente con lo stato della simulazione

---

### Naman Bagga

**Modellazione dell’ambiente simulato:**
- gestione delle celle speciali (cibo, ostacoli, nido ecc.)
- generazione dinamica e aggiornamento delle risorse
- progettazione delle regole ambientali

**Implementazione del motore della simulazione:**
- loop principale
- gestione del tempo e sincronizzazione degli aggiornamenti
- coordinamento tra agenti e ambiente

**Implementazione della logica di interazione:**
- raccolta e consumo del cibo
- aggiornamento dello stato globale della simulazione
- gestione delle condizioni di evoluzione del sistema

**Contributo alla componente grafica:**
- rendering della griglia e degli elementi statici
- gestione del refresh della GUI
- eventuali controlli base (avvio, pausa, reset simulazione)

---

## Parti condivise

- Progettazione architetturale del sistema
- Definizione delle interfacce tra i componenti
- Testing, debugging e refactoring
- Integrazione finale delle componenti
- Redazione della relazione e documentazione del codice  
