
package test.java.com.shop.tests;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class CartFunctionalTest {

    private WebDriver driver;
    private WebDriverWait wait;

    private static final String BASE_URL = "http://localhost";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
    }

    // Método auxiliar: iniciar sesión
    
    private void login() {

        System.out.println("LOGIN: Abriendo formulario...");
        driver.get(BASE_URL + "/login");

        WebElement correo = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("email")
            )
        );

        System.out.println("LOGIN: Escribiendo correo...");
        correo.clear();
        correo.sendKeys("customer1@email.com");

        WebElement password = driver.findElement(
            By.id("password")
        );

        System.out.println("LOGIN: Escribiendo contraseña...");
        password.clear();
        password.sendKeys("123");

        WebElement botonLogin = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' and contains(., 'Sign In')]")
            )
        );

        System.out.println("LOGIN: Presionando Sign In...");
        botonLogin.click();

        System.out.println("LOGIN: Esperando autenticación...");

        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[contains(., 'Sign Out')]")
            )
        );

        System.out.println("LOGIN: Autenticación completada.");
        System.out.println("URL actual: " + driver.getCurrentUrl());
    }


    // Método auxiliar: agregar el producto B0001
    private void agregarProducto(int cantidad) {
            driver.get(BASE_URL + "/product/B0001");

            WebElement campoCantidad = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                    By.name("count")
                )
            );

            campoCantidad.clear();
            campoCantidad.sendKeys(String.valueOf(cantidad));

            WebElement botonAgregar = wait.until(
                ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[@type='submit' and contains(., 'Add to Cart')]")
                )
            );

            botonAgregar.click();
        }
    private void abrirCarrito() {
        WebElement enlaceCarrito = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/cart']")
            )
        );

        enlaceCarrito.click();

        wait.until(
            ExpectedConditions.urlContains("/cart")
        );
    }
    
    private void vaciarCarrito() {

        abrirCarrito();

        By botonesRemove = By.xpath(
            "//a[normalize-space(.)='Remove']"
        );

        // Mientras existan productos, eliminarlos
        while (!driver.findElements(botonesRemove).isEmpty()) {

            WebElement botonRemove = wait.until(
                ExpectedConditions.elementToBeClickable(
                    botonesRemove
                )
            );

            botonRemove.click();

            // Esperar que disminuya el número de productos
            wait.until(
                ExpectedConditions.stalenessOf(botonRemove)
            );
        }

        System.out.println("Carrito vacío. Preparación completada.");
    }

    // CP-01: Agregar producto al carrito
    @Test
    void agregarProductoAlCarrito() {

        // 1. Autenticarse
        login();

        // Preparar estado inicial
        vaciarCarrito();

        // Ejecutar el escenario
        agregarProducto(1);

        // 3. Entrar al carrito
        WebElement enlaceCarrito = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/cart']")
            )
        );

        enlaceCarrito.click();

        // 4. Comprobar que el producto aparece
        // Esperar hasta que aparezca el producto con el texto correcto
        WebElement producto = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[@href='/product/B0001' and normalize-space(.)='Core Java']")
            )
        );

        // Comprobar que estamos dentro del carrito
        assertTrue(driver.getCurrentUrl().contains("/cart"));

        // Comprobar el nombre del producto
        assertEquals("Core Java", producto.getText().trim());

        System.out.println("CP-01: Producto agregado correctamente");
    }
    
        
    @Test
    void aumentarCantidadProducto() {

        // 1. Iniciar sesión
        login();

        // Preparación independiente
        vaciarCarrito();
        agregarProducto(1);

        // Navegar al carrito
        abrirCarrito();

        // 3. Entrar al carrito
        // 3. Entrar al carrito mediante Angular
        WebElement enlaceCarrito = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.cssSelector("a[href='/cart']")
            )
        );

        enlaceCarrito.click();

        // Esperar que la navegación termine
        wait.until(
            ExpectedConditions.urlContains("/cart")
        );

        // Esperar que Core Java aparezca en el carrito
        wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                    "//a[@href='/product/B0001' and normalize-space(.)='Core Java']"
                )
            )
        );

        // 4. Buscar el campo de cantidad
        WebElement campoCantidad = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("B0001")
            )
        );

        // 5. Leer cantidad inicial
        int cantidadInicial = Integer.parseInt(
            campoCantidad.getAttribute("value")
        );

        System.out.println("Cantidad inicial: " + cantidadInicial);

        // 6. Presionar +
        WebElement botonMas = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[.//i[contains(@class,'fa-plus')]]")
            )
        );

        botonMas.click();

        // 7. Esperar a que aumente la cantidad
        wait.until(
            ExpectedConditions.attributeToBe(
                By.id("B0001"),
                "value",
                String.valueOf(cantidadInicial + 1)
            )
        );

        // 8. Verificar resultado
        WebElement cantidadActual = driver.findElement(
            By.id("B0001")
        );

        int cantidadFinal = Integer.parseInt(
            cantidadActual.getAttribute("value")
        );

        assertEquals(cantidadInicial + 1, cantidadFinal);

        System.out.println("Cantidad final: " + cantidadFinal);
        System.out.println("CP-02: Cantidad aumentada correctamente");
    }
    
    @Test
    void disminuirCantidadProducto() {

        // 1. Iniciar sesión
        login();

        // 2. Preparar el carrito vacío
        vaciarCarrito();

        // 3. Agregar dos unidades de Core Java
        agregarProducto(2);

        // 4. Entrar al carrito
        abrirCarrito();

        // 5. Localizar el campo de cantidad
        By campoCantidad = By.id("B0001");

        WebElement cantidad = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                campoCantidad
            )
        );

        int cantidadInicial = Integer.parseInt(
            cantidad.getDomProperty("value")
        );

        // Verificar la precondición
        assertEquals(2, cantidadInicial);

        // 6. Localizar y presionar el botón -
        WebElement botonMenos = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[.//i[contains(@class,'fa-minus')]]")
            )
        );

        botonMenos.click();

        // 7. Esperar hasta que la cantidad sea 1
        wait.until(
            ExpectedConditions.attributeToBe(
                campoCantidad,
                "value",
                "1"
            )
        );

        // 8. Verificar el resultado
        WebElement cantidadActual = driver.findElement(
            campoCantidad
        );

        int cantidadFinal = Integer.parseInt(
            cantidadActual.getDomProperty("value")
        );

        assertEquals(1, cantidadFinal);

        System.out.println("Cantidad inicial: " + cantidadInicial);
        System.out.println("Cantidad final: " + cantidadFinal);
        System.out.println("CP-03: Cantidad disminuida correctamente");
    }

    @Test
    void eliminarProductoDelCarrito() {

        // 1. Iniciar sesión
        login();

        // 2. Preparar el carrito vacío
        vaciarCarrito();

        // 3. Agregar Core Java
        agregarProducto(1);

        // 4. Entrar al carrito
        abrirCarrito();

        // 5. Comprobar que existe el producto
        By producto = By.xpath(
            "//a[@href='/product/B0001' and normalize-space(.)='Core Java']"
        );

        wait.until(
            ExpectedConditions.visibilityOfElementLocated(producto)
        );

        // 6. Presionar Remove
        WebElement botonRemove = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//a[normalize-space(.)='Remove']")
            )
        );

        botonRemove.click();

        // 7. Esperar que desaparezca el producto
        wait.until(
            ExpectedConditions.invisibilityOfElementLocated(producto)
        );

        // 8. Verificar que ya no existe
        assertTrue(
            driver.findElements(producto).isEmpty(),
            "Core Java todavía aparece en el carrito"
        );

        System.out.println("CP-04: Producto eliminado correctamente");
    }
    
    @Test
    void verificarTotalCarrito() {

        // 1. Iniciar sesión
        login();

        // 2. Preparar carrito vacío
        vaciarCarrito();

        // 3. Agregar dos unidades de Core Java
        agregarProducto(2);

        // 4. Entrar al carrito
        abrirCarrito();

        // 5. Verificar que la cantidad sea 2
        WebElement campoCantidad = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("B0001")
            )
        );

        assertEquals(
            "2",
            campoCantidad.getDomProperty("value")
        );

        // 6. Localizar el total del carrito
        By selectorTotal = By.xpath(
            "//h5[starts-with(normalize-space(.), 'Total:')]"
        );

        // 7. Esperar el total esperado
        wait.until(driver -> {
            String texto = driver.findElement(selectorTotal).getText();
            return texto.contains("$60.00");
        });

        // 8. Obtener el texto del total
        WebElement elementoTotal = driver.findElement(
            selectorTotal
        );

        String totalMostrado = elementoTotal.getText().trim();

        // 9. Verificar el resultado
        assertEquals("Total: $60.00", totalMostrado);

        System.out.println("Total obtenido: " + totalMostrado);
        System.out.println("CP-05: Total calculado correctamente");
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
    

}
