import controllers.routes
import org.junit._
import play.mvc._
import play.test.Helpers._
import org.fest.assertions.Assertions._

/**
 *
 * Simple (JUnit) tests that can call all parts of a Play app.
 * If you are interested in mocking a whole application, see the wiki for more details.
 *
 */
class MainControllerTest {
  @Test def simpleCheck {
    val a: Int = 1 + 1
    assertThat(a).isEqualTo(2)
  }

  @Test def indexTemplateShouldContainTheStringThatIsPassedToIt {
    running(fakeApplication, new Runnable {
      def run {
        val html: Content = views.html.index.render
        assertThat(contentType(html)).isEqualTo("text/html")
        assertThat(contentAsString(html)).contains("Your new application is ready.")
      }
    })
  }

  @Test def indexShouldContainTheCorrectString {
    running(fakeApplication, new Runnable {
      def run {
        val result: Result = callAction(routes.ref.MainController.index)
        assertThat(status(result)).isEqualTo(OK)
        assertThat(contentType(result)).isEqualTo("text/html")
        assertThat(charset(result)).isEqualTo("utf-8")
        assertThat(contentAsString(result)).contains("Hello from Java")
      }
    })
  }
}