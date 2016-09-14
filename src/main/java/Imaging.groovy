import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageInputStream
import java.awt.image.BufferedImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Imaging {

  def banana = 1

  class SomeTask implements Runnable {

    def index = -1

    SomeTask(Integer i) {
      index = i
    }

    @Override
    public void run() {
      // Convert to PNG
      def image = images[index]
      File outputfile = new File("saved-${index}.png");
      ImageIO.write(image, "png", outputfile);
      print "..${index + 1}"
    }
  }

  def images = []
  def ImageReader imageReader
  def ImageInputStream inputStream

  // Constructor
  Imaging() {
    //    ImageIO.scanForPlugins();

    def myreaders = ImageIO.getReaderFormatNames()
    myreaders.each {println "Reader: $it"}

    def mywriters = ImageIO.getWriterFormatNames()
    mywriters.each {println "Writer: $it"}

    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
    if (null == writers || !writers.hasNext()) {
      throw new Exception("Appropriate Tiff writer not found");
    }
  }


  // Done sequentially
  // Load then save, load then save
  def convertImagesStd(filename) {
    ImageInputStream is = ImageIO.createImageInputStream(new File(filename));
    Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);

    if (iterator == null || !iterator.hasNext()) {
      throw new IOException("Image file format not supported by ImageIO: " + filename);
    }
    // We are just looking for the first reader compatible:
    ImageReader reader = (ImageReader) iterator.next()
    reader.setInput(is)

    def nbPages = reader.getNumImages(true);
    println "There are $nbPages pages in TIFF file"

    print "Processing image.."
    (0..nbPages - 1).each {
      // Load TIFF
      def image = reader.read(it)
      images.add(image)
      print "L..${it + 1}"

      // Convert to PNG
      File outputfile = new File("saved-${it}.png");
      ImageIO.write(image, "png", outputfile);
      print "S..${it + 1}"
    }
    println()

  }

  // load all then save all
  def convertImagesSequentially(filename) {

    ImageInputStream is = ImageIO.createImageInputStream(new File(filename));
    Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);

    if (iterator == null || !iterator.hasNext()) {
      throw new IOException("Image file format not supported by ImageIO: " + filename);
    }
    // We are just looking for the first reader compatible:
    ImageReader reader = (ImageReader) iterator.next()
    reader.setInput(is)

    def nbPages = reader.getNumImages(true);
    println "There are $nbPages pages in TIFF file"

    print "Loaded image"
    (0..nbPages - 1).each {

      // Load TIFF
      def image = reader.read(it)
      images.add(image)
      print "..${it + 1}"
    }
    println()

    print "Saved image"

    (0..nbPages - 1).each {
      def image = images[it]

      // Convert to PNG
      File outputfile = new File("saved-${it}.png");
      ImageIO.write(image, "png", outputfile);
      print "..${it + 1}"
    }
    println()

  }

  def convertImagesThreadedSave(filename) {
    ImageInputStream is = ImageIO.createImageInputStream(new File(filename));
    Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);

    if (iterator == null || !iterator.hasNext()) {
      throw new IOException("Image file format not supported by ImageIO: " + filename);
    }
    // We are just looking for the first reader compatible:
    ImageReader reader = (ImageReader) iterator.next()
    reader.setInput(is)
    inputStream = is

    def nbPages = reader.getNumImages(true);
    println "There are $nbPages pages in TIFF file"

    print "Loaded image"
    imageReader = reader
    (0..nbPages - 1).each {

      // Load TIFF
      def image = reader.read(it)
      images.add(image)
      print "..${it + 1}"
    }
    println()

    // try doing with executor service for speed experiment
    ExecutorService executor = Executors.newCachedThreadPool()
    (0..nbPages - 1).each {
      executor.execute(new SomeTask(it))
    }

    executor.shutdown()
    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    println()
  }

  def loadTiff(filename) {
    // Reads given tiff into separate image pages

    ImageInputStream is = ImageIO.createImageInputStream(new File(filename));
    Iterator<ImageReader> iterator = ImageIO.getImageReaders(is);

    if (iterator == null || !iterator.hasNext()) {
      throw new IOException("Image file format not supported by ImageIO: " + filename);
    }
    // We are just looking for the first reader compatible:
    ImageReader reader = (ImageReader) iterator.next()
    reader.setInput(is)
    inputStream = is

    def nbPages = reader.getNumImages(true);
    println "There are $nbPages pages in TIFF file"

    imageReader = reader
    (0..nbPages - 1).each {
      images.add(null)

      //      // Load TIFF
      //      def image = reader.read(it)
      //      images.add(image)
      //      println "Loaded image ${it+1}"

      //      // Convert to PNG
      //      File outputfile = new File("saved-${it}.png");
      //      ImageIO.write(image, "png", outputfile);
      //      println "Saved image ${it+1}"

    }

    //    (0..nbPages-1).each {
    //      def image = images[it]
    //
    //      // Convert to PNG
    //      File outputfile = new File("saved-${it}.png");
    //      ImageIO.write(image, "png", outputfile);
    //      println "Saved image ${it+1}"
    //    }

    // try doing with executor service for speed experiment
    ExecutorService executor = Executors.newCachedThreadPool()
    (0..nbPages - 1).each {
      executor.execute(new SomeTask(it))
      //      Thread.sleep(500)
    }

    executor.shutdown()
  }
}
