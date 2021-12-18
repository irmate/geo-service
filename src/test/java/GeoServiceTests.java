import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.netology.entity.Country;
import ru.netology.entity.Location;
import ru.netology.geo.GeoService;
import ru.netology.geo.GeoServiceImpl;
import ru.netology.i18n.LocalizationService;
import ru.netology.i18n.LocalizationServiceImpl;
import ru.netology.sender.MessageSender;
import ru.netology.sender.MessageSenderImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class GeoServiceTests {

    @BeforeAll
    public static void initSuite() {
        System.out.println("Running Geo-Service Tests");
    }
    @AfterAll
    public static void completeSuite() {
        System.out.println("Geo-Service Tests completed");
    }
    @BeforeEach
    public void init() {
        System.out.println("Test started");
    }
    @AfterEach
    public void finished() {
        System.out.println("\nTest finished");
    }

    @ParameterizedTest
    @MethodSource("sourse")
    public void test_message_send(String ipAddress, String expected){
        GeoService geoService = Mockito.mock(GeoServiceImpl.class);
        if (ipAddress.startsWith("172.")) {
            Mockito.when(geoService.byIp(ipAddress))
                    .thenReturn(new Location("Moscow", Country.RUSSIA, null, 0));
        } else {
            Mockito.when(geoService.byIp(ipAddress)).
                    thenReturn(new Location("New York", Country.USA, null,  0));
        }

        LocalizationService localizationService = Mockito.mock(LocalizationServiceImpl.class);
        Mockito.when(localizationService.locale(Country.RUSSIA))
                .thenReturn("Добро пожаловать");
        Mockito.when(localizationService.locale(Country.USA))
                .thenReturn("Welcome");

        MessageSender messageSender = new MessageSenderImpl(geoService, localizationService);
        Map<String, String> headers = new HashMap<>();
        headers.put(MessageSenderImpl.IP_ADDRESS_HEADER, ipAddress);
        String preferences = messageSender.send(headers);

        Assertions.assertEquals(expected, preferences);
    }

    public static Stream<Arguments>sourse() {
        return Stream.of(Arguments.of("172.0.32.11", "Добро пожаловать"), Arguments.of("96.44.183.149", "Welcome"));
    }

    @Test
    public void test_identify_location(){
        String ip = "172.0.32.11";
        Country expected = new Location("Moscow", Country.RUSSIA, "Lenina", 15).getCountry();

        GeoService geoService = new GeoServiceImpl();
        Country result = geoService.byIp(ip).getCountry();

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void test_string_return(){
        Country country = Country.USA;
        String expected = "Welcome";

        LocalizationService localizationService = new LocalizationServiceImpl();
        String result = localizationService.locale(country);

        Assertions.assertEquals(expected, result);
    }

    @Test
    public void test_throw_exception(){
        double latitude = 123.44;
        double longitude = 223.44;

        GeoService geoService = new GeoServiceImpl();

        Assertions.assertThrows(RuntimeException.class, () -> geoService.byCoordinates(latitude, longitude));
    }
}