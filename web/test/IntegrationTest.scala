import org.fest.assertions.Assertions.assertThat
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerSuite, OneServerPerSuite, PlaySpec}

class IntegrationTest extends PlaySpec with OneServerPerSuite with OneBrowserPerSuite with HtmlUnitFactory {

  "PlayMovies application" should {

    "run in a browser" in {
      go to ("http://localhost:3333")
      assertThat(pageSource).contains("Hello from Java")
    }
  }
}