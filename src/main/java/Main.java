public class Main {


  public static void main(String[] args) {
    String filename = "/Users/pbartlett/desktop/washington3.tiff";

    Imaging imaging = new Imaging();
    StopWatch sw = new StopWatch();
    imaging.convertImagesStd(filename);
    System.out.printf("Took %d ms to convertImagesStd\n", sw.reset());
    imaging.convertImagesSequentially(filename);
    System.out.printf("Took %d ms to convertImagesSequentially\n", sw.reset());
    imaging.convertImagesThreadedSave(filename);
    System.out.printf("Took %d ms to convertImagesThreadedSave\n", sw.reset());
  }

}

class StopWatch {
  long startTime = System.currentTimeMillis();
  long stop() { return System.currentTimeMillis() - startTime; }
  long reset() { long duration = stop(); startTime = System.currentTimeMillis(); return duration; }
}

