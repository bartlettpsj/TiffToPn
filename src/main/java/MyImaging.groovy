import com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter
import org.apache.commons.imaging.ImageFormat
import org.apache.commons.imaging.ImageFormats
import org.apache.commons.imaging.Imaging
import org.apache.commons.imaging.ImagingConstants
import org.apache.commons.imaging.formats.tiff.constants.TiffConstants
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.rendering.ImageType
import org.apache.pdfbox.rendering.PDFRenderer

import javax.imageio.ImageIO
import javax.imageio.ImageReader
import javax.imageio.ImageWriter
import javax.imageio.stream.ImageInputStream
import java.awt.image.BufferedImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;

import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.SeekableStream;
import com.sun.media.jai.codec.FileSeekableStream;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.TIFFEncodeParam;

class MyImaging {

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
  def TIFFImageWriter tiffWriter

  // Constructor
  MyImaging() {
    def myreaders = ImageIO.getReaderFormatNames()
    myreaders.each {println "Reader: $it"}

    def mywriters = ImageIO.getWriterFormatNames()
    mywriters.each {println "Writer: $it"}

    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
    if (null == writers || !writers.hasNext()) {
      throw new Exception("Appropriate Tiff writer not found");
    }
    tiffWriter = writers.next()
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
      //      images.add(image)
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

    images = []

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
    images = []
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

  def convertImagesToMultiTiff(filename) {
    images = []
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


    // Save to multipage as JPG compression - assumes at least one page!
    TIFFEncodeParam params = new TIFFEncodeParam()
    params.setCompression(TIFFEncodeParam.COMPRESSION_JPEG_TTN2)
    OutputStream out = new FileOutputStream("multi-page.tif")
    ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params)
    List<BufferedImage> list = new ArrayList<BufferedImage>(images.size())
    //      images.each { list.add(it)}
    for (int i = 1; i < images.size(); i++) {
      list.add(images[i]);
    }
    params.setExtraImages(list.iterator());
    encoder.encode(images[0]);
    out.close()
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

  def convertPdfToMultiTiff(String pdfFilename, String tiffFilename) {
    def pdf = PDDocument.load(new File(pdfFilename))
    println pdf.dump()
    PDFRenderer pdfRenderer = new PDFRenderer(pdf);

    (1..pdf.pages.count).each {
      PDPage page = (PDPage) pdf.documentCatalog.pages.get(it-1)

      BufferedImage image = pdfRenderer.renderImageWithDPI(it-1, 200, ImageType.BINARY);
      images.add(image) // could be done easier
    }

    // Save to multipage as JPG compression - assumes at least one page!
    TIFFEncodeParam params = new TIFFEncodeParam()
    params.setCompression(TIFFEncodeParam.COMPRESSION_GROUP4) // COMPRESSION_JPEG_TTN2)
    OutputStream out = new FileOutputStream(tiffFilename)
    ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params)
    List<BufferedImage> list = new ArrayList<BufferedImage>(images.size())
    for (int i = 1; i < images.size(); i++) {
      list.add(images[i]);
    }
    params.setExtraImages(list.iterator());
    encoder.encode(images[0]);
    out.close()





  }
}
