import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

import java.time.Duration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SeleniumNewExamTest {
    WebDriver driver;
    WebDriverWait wait;

    @BeforeAll
    void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setupTest() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    private void pauseForFiveSeconds() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    public void testCreateNewExam() {
        driver.get("http://127.0.0.1:4200");
        WebElement vaoNgayBtn = driver.findElement(By.cssSelector("[href='/login']"));
        Assertions.assertTrue(vaoNgayBtn.getText().contains("Đăng nhập"));
        vaoNgayBtn.click();
        pauseForFiveSeconds();

        WebElement userNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        userNameInput.sendKeys("thanhtam28ss");
        WebElement passWordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passWordInput.sendKeys("123456");
        WebElement loginBtn = driver.findElement(By.xpath("//button[text() = ' Đăng nhập ']"));
        loginBtn.click();
        pauseForFiveSeconds();

        vaoNgayBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[href='/admin']")));
        Assertions.assertTrue(vaoNgayBtn.isDisplayed());
        vaoNgayBtn.click();
        pauseForFiveSeconds();

        WebElement testManagementLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/admin/tests']")));
        testManagementLink.click();
        pauseForFiveSeconds();

        WebElement addNewButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/admin/tests/add-test']")));
        addNewButton.click();
        pauseForFiveSeconds();

        // Fill in the form
        WebElement intakeSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[formcontrolname='intake']")));
        Select intakeDropdown = new Select(intakeSelect);
        intakeDropdown.selectByValue("1");
        pauseForFiveSeconds();

        WebElement testTitleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("test-title")));
        testTitleInput.sendKeys("New Test Title");
        pauseForFiveSeconds();

        WebElement courseSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[name='course']")));
        Select courseDropdown = new Select(courseSelect);
        courseDropdown.selectByValue("12"); // Java Back-End
        pauseForFiveSeconds();

        WebElement partSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("select[name='part']")));
        Select partDropdown = new Select(partSelect);
        partDropdown.selectByValue("74"); // Hibernate/JPA
        pauseForFiveSeconds();

        WebElement timeBeginInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[formcontrolname='timeBegin']")));
        timeBeginInput.sendKeys("2020-04-16 16:36:54");
        pauseForFiveSeconds();

        WebElement timeEndInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[formcontrolname='timeEnd']")));
        timeEndInput.sendKeys("2020-04-17 16:36:54");
        pauseForFiveSeconds();

        WebElement timeDurationInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input[formcontrolname='timeDuration']")));
        timeDurationInput.sendKeys("60");
        pauseForFiveSeconds();

        // Select the 'allCheck' checkbox
        try {
            WebElement allCheckCheckbox = driver.findElement(By.cssSelector("input[name='allCheck']"));
            wait.until(ExpectedConditions.elementToBeClickable(allCheckCheckbox));
            if (!allCheckCheckbox.isSelected()) {
                allCheckCheckbox.click();
            }
        } catch (Exception e) {
            System.out.println("Error selecting 'allCheck' checkbox: " + e.getMessage());
        }
        pauseForFiveSeconds();

        // Uncheck one of the 'noneCheck' checkboxes
        try {
            WebElement noneCheckCheckbox = driver.findElement(By.cssSelector("input[name='noneCheck'][ng-reflect-model='true']"));
            wait.until(ExpectedConditions.elementToBeClickable(noneCheckCheckbox));
            if (noneCheckCheckbox.isSelected()) {
                noneCheckCheckbox.click();
            }
        } catch (Exception e) {
            System.out.println("Error unchecking 'noneCheck' checkbox: " + e.getMessage());
        }
        pauseForFiveSeconds();

        // Submit the form
        WebElement submitButton = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button[type='submit']")));
        submitButton.click();
        pauseForFiveSeconds();
    }

    @Test
    public void testViewExamList() {
        driver.get("http://127.0.0.1:4200");
        WebElement vaoNgayBtn = driver.findElement(By.cssSelector("[href='/login']"));
        Assertions.assertTrue(vaoNgayBtn.getText().contains("Đăng nhập"));
        vaoNgayBtn.click();
        pauseForFiveSeconds();

        WebElement userNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        userNameInput.sendKeys("thanhtam28ss");
        WebElement passWordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        passWordInput.sendKeys("123456");
        WebElement loginBtn = driver.findElement(By.xpath("//button[text() = ' Đăng nhập ']"));
        loginBtn.click();
        pauseForFiveSeconds();

        vaoNgayBtn = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("[href='/admin']")));
        Assertions.assertTrue(vaoNgayBtn.isDisplayed());
        vaoNgayBtn.click();
        pauseForFiveSeconds();

        WebElement testManagementLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a[href='/admin/tests']")));
        testManagementLink.click();
        pauseForFiveSeconds();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
