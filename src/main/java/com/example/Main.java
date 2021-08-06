package com.example;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;


public class Main {
    @ignore
    public static  String hello;
    public static void main(String[] args) {
        Test test = new Test(1 , true , "ali" , "mohamado" , "123");
        Test test1 = new Test(2 , false , "ali1" , "asef" , "12wefwf3");
        Test test2 = new Test(4 , true , "ali2" , "mohamadwfwefo" , "wfwew");
        Test test3 = new Test(5 , false , "ali3" , "weffwef" , "fwefew");
        Test[] tests = {test , test1 , test2 , test3};
        try {
            Generator.writeOnFile("out.txt" , Generator.csvGenerator(tests));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
class Generator {
    public static void writeOnFile(String path , String csv) throws IOException {
        File file = new File(path);
        FileWriter fw = new FileWriter(file);
        PrintWriter pw = new PrintWriter(fw);
        pw.println(csv);

        pw.close();
        fw.close();
    }

    public static String csvGenerator(Object[] objects) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (objects.length == 0)
            return "";
        Object firstObject = objects[0];
        StringBuilder sb = new StringBuilder();
        String firstRow = getFirstRowFieldsInString(firstObject);
        String values = getAllValues(objects);
        sb.append(firstRow).append("\n").append(values);
        return sb.toString();
    }

    private static Field[] getFirstRowFields(Object object) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Field[] fields = object.getClass().getFields();
        Field[] fieldsInOrder = new Field[fields.length];
        for(int i =0 ; i< fields.length; i ++){
            Field field = fields[i];
            if(ignoreCheck(field)){
                fieldsInOrder[i] = null;
            }else if(csvConfigCheck(field)){
                Annotation annotation = field.getAnnotation(CSVConfig.class);
                Method columnIndexMethod = field.getAnnotation(CSVConfig.class).getClass().getMethod("columnIndex");
                int index = (int) columnIndexMethod.invoke(annotation);
                fieldsInOrder[index] = field;
            }
        }
        for(Field field : fields){
            if(!ignoreCheck(field) && !csvConfigCheck(field)){
                int nullIndex = -1;
                for(int i =0 ; i < fieldsInOrder.length ; i++){
                    Field f = fieldsInOrder[i];
                    if(f == null){
                        nullIndex = i;
                        break;
                    }
                }
                fieldsInOrder[nullIndex] = field;
            }
        }
        return fieldsInOrder;
    }
    private static String getFirstRowFieldsInString(Object object) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        StringBuilder sb = new StringBuilder();
        Field[] fields = getFirstRowFields(object);
        for(int i =0 ;  i< fields.length ; i++){
            Field field = fields[i];
            if(field != null){
                String name = field.getName();
                if(csvConfigCheck(field)){
                    Annotation annotation = field.getAnnotation(CSVConfig.class);
                    Method columnNameMethod = field.getAnnotation(CSVConfig.class).getClass().getMethod("columnName");
                    name = (String) columnNameMethod.invoke(annotation);
                }
                if(i == fields.length -2){
                    sb.append(name);
                }else
                sb.append(name).append(" , ");
            }
        }
        return sb.toString();
    }
    private static String getAllValues(Object[] objects) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        if(objects.length == 0)
            return "";
        Object firstObject = objects[0];
        StringBuilder sb = new StringBuilder();
        ArrayList<Field> fieldsInOrder = new ArrayList<>(Arrays.asList(getFirstRowFields(firstObject)));
        Class clazz = firstObject.getClass();
        Field[] fields = clazz.getFields();
        for(Object object : objects) {
            String[] values = new String[fieldsInOrder.size()];
            for (Field field : fields) {
                if(fieldsInOrder.contains(field)){
                    int index = fieldsInOrder.lastIndexOf(field);
                    String value = String.valueOf(field.get(object));
                    values[index] = value;
                }
            }
            for(int i =0 ; i < values.length; i++){
                String value = values[i];
                if(value != null ) {
                    if (i == values.length - 2) {
                        sb.append(values[i]).append("\n");
                    } else {
                        sb.append(values[i]).append(" , ");
                    }
                }
            }
        }

        return sb.toString();
    }
    private static boolean ignoreCheck(Field field){
        return (field.getAnnotation(ignore.class) != null);
    }
    private static boolean csvConfigCheck(Field field){
        return (field.getAnnotation(CSVConfig.class) != null);
    }
}
class Test{
    @ignore
    public int a;
    public boolean isAlive;
    @CSVConfig(columnName = "nam" , columnIndex = 2)
    public String name;
    @CSVConfig(columnName = "family-Name" , columnIndex = 0)
    public String family;
    @CSVConfig(columnName = "Age" , columnIndex = 3)
    public String age;

    public Test(int a, boolean isAlive, String name, String family, String age) {
        this.a = a;
        this.isAlive = isAlive;
        this.name = name;
        this.family = family;
        this.age = age;
    }
}

