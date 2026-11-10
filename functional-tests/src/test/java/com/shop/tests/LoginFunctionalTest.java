
package com.shop.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LoginFunctionalTest {
    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();
        driver.get(BASE_URL + "/login");
    }

    @Test
    void inicioSesionCredencialesValidas() {
        ingresarCredenciales("customer1@email.com", "123");
        WebElement botonLogin = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' and contains(., 'Sign In')]")
            )
        );
        botonLogin.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/product"));
        WebElement signOut = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//a[contains(., 'Sign Out')]"))
        );
        assertEquals(
            BASE_URL + "/product",
            driver.getCurrentUrl(),
            "El usuario no fue redirigido a productos"
        );
        assertTrue(
            signOut.isDisplayed(),
            "No aparece Sign Out despues del login"
        );
    }

    @Test
    void inicioSesionContrasenaIncorrecta() {
        ingresarCredenciales(
            "customer1@email.com",
            "ClaveIncorrecta456"
        );
        pulsarSignInConReintento();
        WebElement mensajeError = esperarMensajeError();
        assertEquals(
            "Invalid username and password.",
            mensajeError.getText().trim(),
            "No se mostro el mensaje esperado"
        );
        assertEquals(
            BASE_URL + "/login",
            driver.getCurrentUrl(),
            "El usuario no deberia salir de Login"
        );
        assertTrue(
            driver.findElements(By.xpath("//a[contains(., 'Sign Out')]")).isEmpty(),
            "Se encontro Sign Out pese al rechazo del login"
        );
    }

    @Test
    void inicioSesionUsuarioInexistente() {
        ingresarCredenciales(
            "usuarioinexistente999@example.com",
            "123"
        );
        pulsarSignInConReintento();
        WebElement mensajeError = esperarMensajeError();
        assertEquals(
            "Invalid username and password.",
            mensajeError.getText().trim(),
            "No se mostro el mensaje de autenticacion fallida"
        );
        assertEquals(
            BASE_URL + "/login",
            driver.getCurrentUrl(),
            "Un usuario inexistente no deberia acceder a productos"
        );
        assertTrue(
            driver.findElements(
                By.xpath("//a[contains(., 'Sign Out')]")
            ).isEmpty(),
            "El sistema muestra Sign Out para un usuario no autenticado"
        );
    }

    @Test
    void inicioSesionCamposVacios() {
        WebElement correo = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("email"))
        );
        WebElement password = driver.findElement(By.id("password"));
        WebElement botonLogin = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//button[@type='submit' and contains(., 'Sign In')]")
            )
        );
        assertEquals(
            "",
            correo.getDomProperty("value"),
            "El campo Email deberia estar vacio"
        );
        assertEquals(
            "",
            password.getDomProperty("value"),
            "El campo Password deberia estar vacio"
        );
        assertFalse(
            botonLogin.isEnabled(),
            "El boton Sign In deberia estar deshabilitado"
        );
        assertEquals(
            BASE_URL + "/login",
            driver.getCurrentUrl(),
            "El usuario deberia permanecer en Login"
        );
        assertTrue(
            driver.findElements(By.xpath("//a[contains(., 'Sign Out')]")).isEmpty(),
            "No deberia existir una sesion activa"
        );
    }

    @Test
    void cerrarSesionCorrectamente() {
        ingresarCredenciales("customer1@email.com", "123");
        WebElement botonLogin = wait.until(
            ExpectedConditions.elementToBeClickable(
                By.xpath("//button[@type='submit' and contains(., 'Sign In')]")
            )
        );
        botonLogin.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/product"));
        WebElement signOut = wait.until(
            ExpectedConditions.elementToBeClickable(By.xpath("//a[contains(., 'Sign Out')]"))
        );
        assertTrue(
            signOut.isDisplayed(),
            "El usuario no inicio sesion correctamente"
        );
        signOut.click();
        wait.until(ExpectedConditions.urlToBe(BASE_URL + "/login?logout=true"));
        WebElement mensajeLogout = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[normalize-space(text())='You have been logged out.']")
            )
        );
        assertEquals(
            "You have been logged out.",
            mensajeLogout.getText().trim(),
            "No se mostro el mensaje de cierre de sesion"
        );
        assertEquals(
            BASE_URL + "/login?logout=true",
            driver.getCurrentUrl(),
            "No se redirigio correctamente al Login"
        );
        assertTrue(
            driver.findElements(
                By.xpath("//a[contains(., 'Sign Out')]")
            ).isEmpty(),
            "Sign Out sigue visible despues de cerrar sesion"
        );
        WebElement signIn = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//a[normalize-space(.)='Sign In']")
            )
        );
        assertTrue(
            signIn.isDisplayed(),
            "No aparece Sign In despues del Logout"
        );
    }

    private void ingresarCredenciales(String email, String contrasena) {
        WebElement correo = wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.id("email")
            )
        );
        correo.sendKeys(email);
        WebElement password = driver.findElement(
            By.id("password")
        );
        password.sendKeys(contrasena);
    }

    private void pulsarSignInConReintento() {
        By selectorBoton = By.xpath("//button[@type='submit' and contains(., 'Sign In')]");
        By selectorError = By.xpath("//*[normalize-space(text())='Invalid username and password.']");
        WebElement boton = wait.until(ExpectedConditions.elementToBeClickable(selectorBoton));
        boton.click();
        try {
            new WebDriverWait(driver,Duration.ofSeconds(3)).until(ExpectedConditions.visibilityOfElementLocated(selectorError));
        } catch (TimeoutException e) {
            boton = wait.until(ExpectedConditions.elementToBeClickable(selectorBoton));
            boton.click();
        }
    }

    private WebElement esperarMensajeError() {
        return wait.until(
            ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//*[normalize-space(text())='Invalid username and password.']")
            )
        );
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
