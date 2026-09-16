import feign.RequestTemplate;
public class Test {
    public static void main(String[] args) {
        RequestTemplate template = new RequestTemplate();
        template.target("http://wallet-service");
        template.uri("/api/v1/internal/wallets");
        System.out.println("url=" + template.url());
        System.out.println("path=" + template.path());
    }
}
