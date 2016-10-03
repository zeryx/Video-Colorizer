package ColorizeVideo
import com.google.gson.Gson


/**
  * Created by james on 21/07/16.
  */



case class CombineInputs(uuids: java.util.List[String],
                         inputCollections: java.util.List[String]
                        )

case class CombineFromSubs(input: CombineInputs,
                           output: Outputs,
                           fps: Double)

case class CombineFromImages(input: CombineInputs,
                             output: Outputs,
                             fps: Double)

case class SplittingInputs(inputVideoUrl: String)

case class SplitToImages(input: SplittingInputs,
                         output: Outputs,
                         fps: Double)

case class SplitToSubs(input: SplittingInputs,
                       output: Outputs)

case class Outputs(collection: String,
                   prefix: String,
                   extension: String,
                   zippedOutput: Boolean)


case class Exit(uuid: String, files: java.util.List[String])

object Exit{
  val gson = new Gson()
  def apply(input: String): Exit = {
    gson.fromJson[Exit](input, classOf[Exit])
  }
}