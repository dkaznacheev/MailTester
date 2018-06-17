import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;

import static org.junit.Assert.fail;

public class MailTester {

    private String findFirstEmail(WebDriver driver) {
        List<WebElement> emails;

        boolean hasNextPage = true;
        while (hasNextPage) {
            try {
                emails = driver.findElements(By.xpath("//div[contains(@class, 'b-messages__message_unread')]//span[@class='b-messages__subject']"));
                if (emails.size() == 0) {
                    throw new NoSuchElementException();
                }
                return emails.get(0).getText();
            } catch (NoSuchElementException e) {
                if (driver.findElements(By.className("b-pager__next")).size() > 0) {
                    WebElement nextButton = driver.findElement(By.className("b-pager__next"));
                    nextButton.click();
                } else {

                    hasNextPage = false;
                }
            }
        }
        return null;
    }

    @Test
    public void runTest() {
        Properties properties = new Properties();
        String login;
        String password;
        String chromeDriverPath;

        try (InputStream in = this.getClass().getResourceAsStream("mailtester.config")){
            properties.load(in);
            login = properties.getProperty("login");
            password = properties.getProperty("password");
            chromeDriverPath = properties.getProperty("chromeDriverPath");

            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            WebDriver driver = new ChromeDriver();
            driver.get("https://passport.yandex.ru/auth?mode=auth&retpath=https%3A%2F%2Fmail.yandex.ru%2Flite");
            WebElement loginInput = driver.findElement(By.name("login"));
            WebElement passwordInput = driver.findElement(By.name("passwd"));
            loginInput.sendKeys(login);
            passwordInput.sendKeys(password);
            passwordInput.submit();

            WebDriverWait wait = new WebDriverWait(driver, 60);// 1 minute
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("b-page")));

            String email = findFirstEmail(driver);

            if (email != null) {
                System.out.println(email);
            } else {
                System.out.println("No unread emails found!");
            }

        } catch (IOException e) {
            System.err.println("Invalid configs, check test/resources/mailtester.config");
            fail();
        }
    }
}