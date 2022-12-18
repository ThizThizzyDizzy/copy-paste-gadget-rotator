import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;
public class Main {
    public static void main(String[] args) throws IOException{
        if(args.length==0||args[0].equals("?")||args[0].equals("-?")||args[0].equals("help")||args[0].equals("-h")||args[0].equals("-help")){
            System.out.println("Please provide two arguments; filepath, and the axes to flip.\nFilepath can be absolute or local, in whatever format java will accept\nAxes can either be (x|y|z) to mirror upon a single axis, or (xy|xz|yz) to flip two axes (rotates it)");
        }
        if(args.length>0&&args.length!=2){
            System.err.println("Invalid arguments");
            return;
        }
        File f = new File(args.length>0?args[0]:getFile());
        Operation op = (args.length>0?Operation.valueOf(args[1].toUpperCase(Locale.ROOT)):getOp());
        if(op==null||!f.exists()){
            System.err.println("Invalid arguments");
            return;
        }
        System.out.println("Reading File...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String line;
        String text = "";
        while((line = reader.readLine())!=null)text+=line+"\n";
        reader.close();
        System.out.println("Scannig...");
        int[] startPos = get(text, "startPos");
        int[] endPos = get(text, "endPos");
        int[] pos = getIArr(text, "posIntArray");
        System.out.println("Starting operation: "+op.toString());
        switch (op) {//flip start/end positions
            case XY:
                int a = startPos[0];
                int b = startPos[1];
                startPos[0] = b;
                startPos[1] = a;
                a = endPos[0];
                b = endPos[1];
                endPos[0] = b;
                endPos[1] = a;
                break;
            case XZ:
                a = startPos[0];
                b = startPos[2];
                startPos[0] = b;
                startPos[2] = a;
                a = endPos[0];
                b = endPos[2];
                endPos[0] = b;
                endPos[2] = a;
                break;
            case YZ:
                a = startPos[1];
                b = startPos[2];
                startPos[1] = b;
                startPos[2] = a;
                a = endPos[1];
                b = endPos[2];
                endPos[1] = b;
                endPos[2] = a;
                break;
        }
        int xSize = endPos[0]-startPos[0];
        int ySize = endPos[1]-startPos[1];
        int zSize = endPos[2]-startPos[2];
        for(int i = 0; i<pos.length; i++){
            int[] coords = decode(pos[i]);
            int x = coords[0];
            int y = coords[1];
            int z = coords[2];
            switch (op) {//adjust coords
                case X:
                    x = (xSize-(coords[0]-startPos[0]))+startPos[0];
                    break;
                case Y:
                    y = (ySize-(coords[1]-startPos[1]))+startPos[1];
                    break;
                case Z:
                    z = (zSize-(coords[2]-startPos[2]))+startPos[2];
                    break;
                case XY:
                    x = coords[1];
                    y = coords[0];
                    break;
                case XZ:
                    x = coords[2];
                    z = coords[0];
                    break;
                case YZ:
                    y = coords[2];
                    z = coords[1];
                    break;
            }
            System.out.println(coords[0]+" "+coords[1]+" "+coords[2]+" > "+x+" "+y+" "+z);
            pos[i] = encode(x, y, z);
        }
        String newStartPos = "startPos:{X:"+startPos[0]+",Y:"+startPos[1]+",Z:"+startPos[2]+"}";
        String newEndPos = "endPos:{X:"+endPos[0]+",Y:"+endPos[1]+",Z:"+endPos[2]+"}";
        String newPos = "posIntArray:[I;";
        for(int i : pos)newPos+=i+",";
        newPos = newPos.substring(0, newPos.length()-1)+"]";
        System.out.println(find(text, "startPos")+" > "+newStartPos);
        System.out.println(find(text, "endPos")+" > "+newEndPos);
        System.out.println(findIArr(text, "posIntArray")+"\nVVV\n"+newPos);
        text = text.replace(find(text, "startPos"), newStartPos);
        text = text.replace(find(text, "endPos"), newEndPos);
        text = text.replace(findIArr(text, "posIntArray"), newPos);
        System.out.println("Done modifying! Writing to "+f.getAbsolutePath());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f)));
        writer.write(text);
        writer.close();
        System.out.println("Write complete!");
    }
    private static String find(String text, String find){
        String str = text.substring(text.indexOf(find+":{"));
        str = str.substring(0, str.indexOf("}")+1);
        return str;
    }
    private static String findIArr(String text, String find){
        String str = text.substring(text.indexOf(find+":[I;"));
        str = str.substring(0, str.indexOf("]")+1);
        return str;
    }
    private static int[] getIArr(String text, String find){
        String str = text.substring(text.indexOf(find+":[I;")+find.length()+4);
        str = str.substring(0, str.indexOf("]"));
        String[] poses = str.split(",");
        int[] pos = new int[poses.length];
        for(int i = 0; i<poses.length; i++){
            pos[i] = Integer.parseInt(poses[i]);
        }
        return pos;
    }
    private static int[] get(String text, String find){
        String str = text.substring(text.indexOf(find+":{")+find.length()+2);
        str = str.substring(0, str.indexOf("}"));
        String[] poses = str.split(",");
        int[] pos = new int[poses.length];
        for(int i = 0; i<poses.length; i++){
            pos[i] = Integer.parseInt(poses[i].substring(2));
        }
        return pos;
    }
    private static String getFile(){
        return javax.swing.JOptionPane.showInputDialog(null, "Enter filepath (you can also do this with a command line argument)", "Enter filepath");
    }

    private static int[] decode(int i) {
        return new int[]{
            (i>>16)&0xFF,
            (i>>8)&0xFF,
            (i)&0xFF
        };
    }
    private static int encode(int x, int y, int z) {
        int px = (x&0xFF)<<16;
        int py = (y&0xFF)<<8;
        int pz = z&0xFF;
        return px|py|pz;
    }
    private static Operation getOp(){
        return Operation.values()[javax.swing.JOptionPane.showOptionDialog(null, "Choose an operation", "Choose Operation", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE, null, Operation.values(), Operation.X)];
    }
    private enum Operation{
        X,
        Y,
        Z,
        XY,
        XZ,
        YZ;
        @Override
        public String toString(){
            return "Flip "+super.toString();
        }
    }
}