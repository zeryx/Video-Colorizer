import org.scalatest.FunSuite
import ColorizeVideo.{ColorizeVideo, Input}
import com.google.gson.Gson
import com.algorithmia._
/**
  * Created by james on 13/06/16.
  */
class  ColorizeTest extends FunSuite {


  test("colorize") {
    val in = "data://zeryx/Video/shorter_porn_test.mp4"
    val inputs: Input = Input(in, 25)
    val data = new ColorizeVideo
    val response = data.apply(inputs, 20)
    Console.println(response)
  }
}
