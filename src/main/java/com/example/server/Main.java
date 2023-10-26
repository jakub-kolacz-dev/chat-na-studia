package com.example.server;

public class Main {

    public static void main(String[] args) {
        FasadaECommerce fasada = new FasadaECommerce();
        String wynik = fasada.zlozZamowienie(123, 456, 1, "karta", "ul. Kwiatowa 7, 00-999 Warszawa");
        System.out.println(wynik);
    }


    static class ZarzadzanieTowarem {
        public boolean sprawdzDostepnosc(int produktId) {
            return true;
        }
    }


    static class ZarzadzanieZamowieniami {
        public int stworzZamowienie(int uzytkownikId, int produktId, int ilosc) {
               return 1;
        }
    }

    static class Platnosci {
        public boolean przetworzPlatnosc(int uzytkownikId, double kwota, String metodaPlatnosci) {
            return true;
        }
    }


    static class Logistyka {
        public boolean zaplanujDostawe(int zamowienieId, String adresDostawy) {

            return true;
        }
    }

    // Fasada
    static class FasadaECommerce {
        private ZarzadzanieTowarem zarzadzanieTowarem;
        private ZarzadzanieZamowieniami zarzadzanieZamowieniami;
        private Platnosci platnosci;
        private Logistyka logistyka;

        public FasadaECommerce() {
            this.zarzadzanieTowarem = new ZarzadzanieTowarem();
            this.zarzadzanieZamowieniami = new ZarzadzanieZamowieniami();
            this.platnosci = new Platnosci();
            this.logistyka = new Logistyka();
        }

        public String zlozZamowienie(int uzytkownikId, int produktId, int ilosc, String metodaPlatnosci, String adresDostawy) {
            if (!zarzadzanieTowarem.sprawdzDostepnosc(produktId)) {
                return "Produkt niedostępny";
            }

            int zamowienieId = zarzadzanieZamowieniami.stworzZamowienie(uzytkownikId, produktId, ilosc);
            if (zamowienieId == 0) {
                return "Nie można utworzyć zamówienia";
            }

            double kwota = ilosc * 100.0;
            if (!platnosci.przetworzPlatnosc(uzytkownikId, kwota, metodaPlatnosci)) {
                return "Płatność nie powiodła się";
            }

            if (!logistyka.zaplanujDostawe(zamowienieId, adresDostawy)) {
                return "Nie można zaplanować dostawy";
            }

            return "Zamówienie " + zamowienieId + " zostało pomyślnie złożone i jest w trakcie realizacji!";
        }
    }
}
