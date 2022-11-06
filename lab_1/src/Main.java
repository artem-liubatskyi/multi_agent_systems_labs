public class Main {
    public static void main(String[] args) {
        String[] jadeArgs = {"-gui", "-agents", "buyer:BookBuyerAgent; seller:BookSellerAgent"};
        jade.Boot.main(jadeArgs);
    }
}