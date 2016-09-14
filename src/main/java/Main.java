public class Main {


  public static void main(String[] args) {
    String filename = "/Users/pbartlett/desktop/washington3.tiff";

    MyImaging myImaging = new MyImaging();
    StopWatch sw = new StopWatch();
    myImaging.convertImagesStd(filename);
    System.out.printf("Took %d ms to convertImagesStd\n", sw.reset());
    myImaging.convertImagesSequentially(filename);
    System.out.printf("Took %d ms to convertImagesSequentially\n", sw.reset());
    myImaging.convertImagesThreadedSave(filename);
    System.out.printf("Took %d ms to convertImagesThreadedSave\n", sw.reset());
    myImaging.convertImagesToMultiTiff(filename);
    System.out.printf("Took %d ms to convertImagesToMultiTiff\n", sw.reset());
  }

}

class StopWatch {
  long startTime = System.currentTimeMillis();
  long stop() { return System.currentTimeMillis() - startTime; }
  long reset() { long duration = stop(); startTime = System.currentTimeMillis(); return duration; }
}

