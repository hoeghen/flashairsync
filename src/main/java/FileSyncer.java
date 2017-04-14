import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by cha on 13-04-2017.
 */
public class FileSyncer {

    private static final String INPUTDIR = "//flashair/dcim";
    private static final String OUTPUTDIR = "D://flashair/sync";
    private static final long DELAY = 5000;
    private Path outputPath;
    static private boolean stop = false;
    private int copied=0;
    private int total=0;
    private long totalSize=0;

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    System.out.println("shutting down");
                    stop=true;
                }
            });
            new FileSyncer().start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() throws InterruptedException, IOException {
        debug("waiting for flashair");
        File inDir  = new File(INPUTDIR);
        debug("from location="+inDir.getAbsolutePath());
        debug("to location="+OUTPUTDIR);
        while(!stop){
            try{
                if(inDir.exists()){
                    outputPath = new File(OUTPUTDIR).toPath();
                    if(!Files.exists(outputPath)){
                        outputPath = Files.createDirectories(new File(OUTPUTDIR).toPath());
                    }
                    if(outputPath.toFile().canWrite()){
                        File[] files = inDir.listFiles();
                        handleFileOrDir(files);
                    }else{
                        debug("cannot write to "+outputPath.getFileName());
                    }
                    debug("files copied "+copied);
                    debug("files total "+total);
                    debug("files total size on card "+totalSize + " Mb");
                }else{
                    System.out.print(".");
                }
            }catch (Exception ex){
                System.out.println(ex);
            }
            Thread.sleep(DELAY);
        }


    }

    private void debug(String message) {
        System.out.println(message);
    }

    private void handleFileOrDir(File[] files) throws IOException {
        for (File file : files) {
            if(file.isDirectory()){
                debug("Dir found"+file.getAbsolutePath());
                handleFileOrDir(file.listFiles());
            }else if(file.isFile()){
                copyThenDelete(file);
            }else{
                debug("could not handle file: "+file.getAbsolutePath());
            }
        }
    }

    private void copyThenDelete(File file) throws IOException {
        if(file.exists() && !file.getName().equals("FA000001.JPG")){
            total++;
            long filesizeMb = file.length() / 1024 / 1024;
            totalSize += filesizeMb;
            Path outfile = Paths.get(outputPath.toString(), file.getName());
            try{
                Files.copy(file.toPath(), outfile);
                copied++;
            }catch(FileAlreadyExistsException ex){
                // thats fine
            }
//            if(Files.exists(outfile)){
//                Files.delete(file.toPath());
//                if(Files.exists(file.toPath())){
//                    throw new RuntimeException("failed deletion of file from source = "+file.getAbsolutePath());
//                }else{
//                    debug("file deleted from source = "+file.getAbsolutePath());
//                }
//            }
        }
    }

}
