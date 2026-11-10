package com.shop.tests;

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
}