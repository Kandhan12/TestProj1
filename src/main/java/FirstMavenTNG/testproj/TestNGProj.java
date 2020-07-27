package FirstMavenTNG.testproj;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.relevantcodes.extentreports.ExtentReports;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

import util.TestUtil;

public class TestNGProj {
	public WebDriver driver;
	public ExtentReports extent;
	public ExtentTest extentTest;

	@BeforeTest
	public void setExtent() {
		extent = new ExtentReports(System.getProperty("user.dir") + "/Screenshots/ExtentReport.html", true);
		extent.addSystemInfo("Host Name", "Kandhan");
		extent.addSystemInfo("User Name", " Kandhan Extent Reports");
		extent.addSystemInfo("Environment", "QA");
	}

	@AfterTest
	public void endReport() {
		extent.flush();
		extent.close();
	}

	public String getScreenshot(WebDriver driver, String screenshotName) {
		String timestamp = new SimpleDateFormat("yyyy_MM_dd__hh_mm_ss").format(new Date());
		File scrfile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
		String destination = System.getProperty("user.dir") + "/Screenshots/" + screenshotName + timestamp + ".png";
		File finaldestination = new File(destination);
		try {
			FileUtils.copyFile(scrfile, finaldestination);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return destination;

	}

	@BeforeMethod
	public void setUp() {
		System.setProperty("webdriver.chrome.driver",
				"C:\\Users\\kandh\\eclipse-workspace\\Excel_Read\\drivers\\chromedriver.exe");
		driver = new ChromeDriver();
		driver.manage().window().maximize();
		driver.manage().deleteAllCookies();
		driver.manage().timeouts().pageLoadTimeout(30, TimeUnit.SECONDS);
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
		driver.get("http://www.demo.guru99.com/V4/");
	}

	@DataProvider
	public Object[][] getLoginData() {
		Object data[][] = TestUtil.getTestData("Login");
		return data;
	}

	@Test(dataProvider = "getLoginData")
	public void loginValidTest(String username, String password) throws InterruptedException {
		String actualBoxMsg;
		extentTest = extent.startTest("LoginValidTest");
		driver.findElement(By.name("uid")).clear();
		driver.findElement(By.name("uid")).sendKeys(username);
		driver.findElement(By.name("password")).clear();
		driver.findElement(By.name("password")).sendKeys(password);
		driver.findElement(By.name("btnLogin")).click();
		Thread.sleep(2000);

		try {
			Alert alt = driver.switchTo().alert();
			actualBoxMsg = alt.getText();
			System.out.println(" actualBoxMsg " + actualBoxMsg);
			assertEquals(actualBoxMsg, "User or Password is not valid12");

		} catch (NoAlertPresentException Ex) {

			// Get text displayes on login page
			String pageText = driver.findElement(By.tagName("tbody")).getText();
			System.out.println("pageText " + pageText);

			// Extract the dynamic text mngrXXXX on page
			String[] parts = pageText.split(":");
			String dynamicText = parts[1];
			System.out.println("dynamicText " + dynamicText + " length " + dynamicText.length());

			// Check that the dynamic text is of pattern mngrXXXX
			// First 4 characters must be "mngr"
			assertTrue(dynamicText.substring(1, 5).equals("mngr"));
			// remain stores the "XXXX" in pattern mngrXXXX
			String remain = dynamicText.substring(dynamicText.length() - 4);
			System.out.println("remain " + remain);
			// Check remain string must be numbers;
			assertTrue(remain.matches("[0-9]+"));
		}

	}

	@AfterMethod
	public void tearDown(ITestResult result) throws IOException {
		if (result.getStatus() == ITestResult.FAILURE) {
			extentTest.log(LogStatus.FAIL, "Test case fail is " + result.getName());
			extentTest.log(LogStatus.FAIL, "Test case fail is " + result.getThrowable());
			String screenshotpath = getScreenshot(driver, result.getName());
			extentTest.log(LogStatus.FAIL, extentTest.addScreenCapture(screenshotpath));
		} else if (result.getStatus() == ITestResult.SKIP) {
			extentTest.log(LogStatus.SKIP, "Test case skip is " + result.getName());
		} else if (result.getStatus() == ITestResult.SUCCESS) {
			extentTest.log(LogStatus.PASS, "Test case pass is " + result.getName());
		}
		extent.endTest(extentTest);
		driver.quit();
	}
}
