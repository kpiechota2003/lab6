Aplikacja okienkowa umożliwia podstawowe operacje przetwarzania obrazu. Została napisana w Javie z użyciem JavaFX.
Program realizuje zadanie z LAB6 na ocenę 4.0.


Dostępne opcje przetwarzania:
  - Konwersja do czerni i bieli - Operacja realizowana metodą applyGrayscale, która wykorzystuje klasę Graphics do przekształcenia obrazu do odcieni szarości (typ BufferedImage.TYPE_BYTE_GRAY).
  - Negatyw - Funkcja applyNegative iteruje po pikselach obrazu i odwraca wartości składowych RGB, tworząc efekt negatywu.
  - Rozmycie - Tymczasowo zaimplementowane jako kopia obrazu bez rzeczywistego przetwarzania (applyBlur). Funkcja służy jako miejsce na przyszłą implementację algorytmu rozmycia (np. filtr Gaussa lub uśredniający).
  - Konturowanie - Operacja wykrywania krawędzi bazująca na różnicach pikseli sąsiadujących w kierunku poziomym i pionowym (applyContour). Powstały efekt uwidacznia kontury obiektów w obrazie.
  - Skalowanie - Opcja skalowania wywołuje nowe okno dialogowe, w którym użytkownik podaje nową szerokość i wysokość obrazu (od 1 do 3000 pikseli). Obraz przeskalowywany jest za pomocą Graphics2D.drawImage.
  - Progowanie - Użytkownik podaje wartość progową z zakresu 0–255, a funkcja applyThreshold binarizuje obraz w oparciu o średnią jasność piksela.
  - Obracanie - Umożliwia obrót obrazu o 90° w prawo lub w lewo. Operacja rotateImage tworzy nowy bufor obrazu z odpowiednio przemapowanymi pikselami.


Dodatkowo obraz w każdej chwili można przywrócić do oryginału klikając "Resetuj obraz". Odbywa się to poprzez stworzenie głębokiej kopii (deepCopy) obrazu oryginalnego.
Możliwe jest wykonanie wielu operacji na obrazie przed jego zapisaniem.

Odczyt i zapis:
Wczytywanie obrazu odbywa się poprzez kliknięcie "Wczytaj" a następnie wybranie odpowiedniego pliku z dysku.  Obraz wczytywany jest przez FileChooser, akceptowane są jedynie pliki .jpg.
Aby zapisać obraz użytkownik jest proszony o podanie nazwy skłądającej się z 3-100 znaków. Program zapobiega nadpisywaniu istniejących plików.

Komunikaty o powodzeniu lub błędach prezentowane są w postaci „tosterów” (Toast.makeText).
