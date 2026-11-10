package com.shop.tests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lab 06 - Pruebas funcionales con Selenium - Funcionalidad: CATÁLOGO DE PRODUCTOS.
 * Requiere la aplicación levantada con docker compose en http://localhost
 * y los datos iniciales de import.sql (se recargan cada vez que se reinicia el backend).
 */
@DisplayName("Pruebas funcionales - Catálogo de productos")
public class CatalogFunctionalTest {

    private static final String BASE_URL = "http://localhost";

    // Selectores sacados de card.component.html, detail.component.html y pagination.component.html
    private static final By TITULO = By.cssSelector("h1.display-4");
    private static final By NOMBRES = By.cssSelector("h4.card-title");
    private static final By PAGINA_ACTIVA =
            By.xpath("//ul[contains(@class,'pagination')]/li[contains(@class,'active')]/button");
    private static final By NUMEROS_DE_PAGINA =
            By.xpath("//ul[contains(@class,'pagination')]/li[normalize-space(.)!='Previous' and normalize-space(.)!='Next']");

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =====================================================================
    // Métodos auxiliares
    // =====================================================================

    /** Abre la página principal. La ruta '' redirige a /product y se muestran 3 productos por página. */
    private void abrirCatalogo() {
        driver.get(BASE_URL + "/");
        wait.until(ExpectedConditions.urlContains("/product"));
        wait.until(ExpectedConditions.textToBe(TITULO, "Get Whatever You Want!"));
        wait.until(ExpectedConditions.numberOfElementsToBe(NOMBRES, 3));
    }

    /** Hace clic en una categoría del menú superior (routerLink, sin recargar la página). */
    private void entrarCategoria(String nombre, int tipo) {
        By enlace = By.xpath("//a[contains(@class,'nav-link') and normalize-space(.)='" + nombre + "']");
        wait.until(ExpectedConditions.elementToBeClickable(enlace)).click();
        wait.until(ExpectedConditions.urlContains("/category/" + tipo));
        wait.until(ExpectedConditions.textToBe(TITULO, nombre));
    }

    /** Hace clic en un enlace habilitado de la paginación (Previous, Next o un número). */
    private void irAPagina(String textoEnlace, int paginaEsperada) {
        By enlace = By.xpath("//ul[contains(@class,'pagination')]/li[not(contains(@class,'disabled'))]"
                + "/a[normalize-space(.)='" + textoEnlace + "']");
        wait.until(ExpectedConditions.elementToBeClickable(enlace)).click();
        wait.until(ExpectedConditions.urlContains("page=" + paginaEsperada));
        // el botón activo cambia cuando llega la nueva página desde el backend
        wait.until(ExpectedConditions.textToBe(PAGINA_ACTIVA, String.valueOf(paginaEsperada)));
    }

    /** Nombres de los productos que se ven en la página actual, en orden. */
    private List<String> nombresVisibles() {
        List<String> nombres = new ArrayList<>();
        for (WebElement h4 : driver.findElements(NOMBRES)) {
            nombres.add(h4.getText().trim());
        }
        return nombres;
    }

    /** Cuerpo de la tarjeta (card-body) del producto con ese nombre. */
    private WebElement tarjeta(String nombreProducto) {
        return driver.findElement(By.xpath("//h4[normalize-space(.)='" + nombreProducto + "']"
                + "/ancestor::div[contains(@class,'card-body')]"));
    }

    private By enlaceDeshabilitado(String texto) {
        return By.xpath("//ul[contains(@class,'pagination')]/li[contains(@class,'disabled')]"
                + "/a[normalize-space(.)='" + texto + "']");
    }

    // =====================================================================
    // Casos de prueba
    // =====================================================================

    @Test
    @DisplayName("CP-01: La página principal muestra la primera página del catálogo")
    void cp01VisualizarCatalogo() {
        // Act
        abrirCatalogo();

        // Assert
        assertEquals(List.of("Core Java", "Spring In Action", "Head First"), nombresVisibles());

        WebElement coreJava = tarjeta("Core Java");
        assertTrue(coreJava.getText().contains("Description:"));
        assertTrue(coreJava.getText().contains("Stock:"));
        assertEquals("Price: $30.00",
                coreJava.findElement(By.xpath(".//p[strong[normalize-space(.)='Price:']]")).getText().trim());
        System.out.println("CP-01: Catálogo visible -> " + nombresVisibles());
    }

        @Test
    @DisplayName("CP-02: El menú permite entrar a las cuatro categorías")
    void cp02EntrarACategorias() {
        abrirCatalogo();

        entrarCategoria("Books", 0);
        assertEquals("Core Java", nombresVisibles().get(0));

        entrarCategoria("Food", 1);
        assertEquals("Chicken", nombresVisibles().get(0));

        entrarCategoria("Clothes", 2);
        assertEquals("T-shirt", nombresVisibles().get(0));

        entrarCategoria("Drink", 3);
        assertEquals("Coca Cola", nombresVisibles().get(0));
        assertTrue(driver.getCurrentUrl().endsWith("/category/3"));
        System.out.println("CP-02: Se entró a Books, Food, Clothes y Drink");
    }

    @Test
    @DisplayName("CP-03: El detalle muestra los datos del producto y calcula el subtotal")
    void cp03ConsultarDetalle() {
        abrirCatalogo();

        // Act: se entra al detalle con el botón "Get It!" de Core Java
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[@href='/product/B0001' and normalize-space(.)='Get It!']"))).click();
        wait.until(ExpectedConditions.urlContains("/product/B0001"));
        wait.until(ExpectedConditions.textToBe(By.cssSelector("h4.card-title"), "Core Java"));

        // Assert: datos del producto con cantidad inicial 1
        assertEquals("Product Detail", driver.findElement(TITULO).getText().trim());
        assertEquals("$30.00", driver.findElement(By.id("price")).getText().trim());
        assertEquals("1", driver.findElement(By.name("count")).getAttribute("value"));
        assertEquals("$30.00", driver.findElement(By.id("subtotal")).getText().trim());

        // Act: se cambia la cantidad a 3
        WebElement cantidad = driver.findElement(By.name("count"));
        cantidad.clear();
        cantidad.sendKeys("3");

        // Assert: subtotal = 3 x $30.00 y el botón para comprar está disponible
        wait.until(ExpectedConditions.textToBe(By.id("subtotal"), "$90.00"));
        assertEquals("$90.00", driver.findElement(By.id("subtotal")).getText().trim());
        assertTrue(driver.findElement(
                By.xpath("//button[@type='submit' and contains(., 'Add to Cart')]")).isEnabled());
        System.out.println("CP-03: Detalle de Core Java correcto, subtotal con 3 unidades = $90.00");
    }

    @Test
    @DisplayName("CP-04: La categoría Books muestra solo sus productos y marca el no disponible")
    void cp04ProductosDeUnaCategoria() {
        abrirCatalogo();
        entrarCategoria("Books", 0);

        // Books tiene 5 productos y se muestran de 3 en 3 -> 2 páginas
        assertEquals(2, driver.findElements(NUMEROS_DE_PAGINA).size());
        List<String> productos = new ArrayList<>(nombresVisibles());

        // Head First (B0003) tiene product_status = 1 -> Unavailable y sin "Get It!"
        WebElement headFirst = tarjeta("Head First");
        WebElement noDisponible = headFirst.findElement(By.xpath(".//a[normalize-space(.)='Unavailable']"));
        assertTrue(noDisponible.getAttribute("class").contains("disabled"));
        assertTrue(headFirst.findElements(By.xpath(".//a[normalize-space(.)='Get It!']")).isEmpty());
        assertFalse(tarjeta("Core Java").findElements(By.xpath(".//a[normalize-space(.)='Get It!']")).isEmpty());

        irAPagina("2", 2);
        productos.addAll(nombresVisibles());

        assertEquals(List.of("Core Java", "Spring In Action", "Head First", "Effective Java", "Thinking in Java"),
                productos);
        System.out.println("CP-04: Books -> " + productos);
    }

    @Test
    @DisplayName("CP-05: La paginación permite navegar entre los productos del catálogo")
    void cp05NavegarEntreProductos() {
        abrirCatalogo();

        // Primera página: 12 productos / 3 por página = 4 páginas, Previous deshabilitado
        assertEquals("1", driver.findElement(PAGINA_ACTIVA).getText().trim());
        assertEquals(4, driver.findElements(NUMEROS_DE_PAGINA).size());
        assertFalse(driver.findElements(enlaceDeshabilitado("Previous")).isEmpty());

        irAPagina("Next", 2);
        assertEquals(List.of("Effective Java", "Thinking in Java", "T-shirt"), nombresVisibles());

        irAPagina("4", 4);
        assertEquals(3, nombresVisibles().size());
        assertEquals("Starbucks Violet Drink", nombresVisibles().get(0));
        assertFalse(driver.findElements(enlaceDeshabilitado("Next")).isEmpty());

        irAPagina("Previous", 3);
        assertEquals(List.of("Shirts", "Coats", "Coca Cola"), nombresVisibles());
        System.out.println("CP-05: Navegación 1 -> 2 -> 4 -> 3 correcta");
    }
}