package com.example.evenue.utils;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.core.converters.ArffSaver;
import java.io.File;

public class HandleNullWithWeka {
    public static void main(String[] args) {
        try {
            // Load the dataset
            DataSource source = new DataSource("/Users/olawale/Desktop/Devs/EVENUE/ML/user_behaviour.csv");
            Instances dataset = source.getDataSet();

            // Handle missing values
            ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
            replaceMissing.setInputFormat(dataset);
            Instances filteredDataset = Filter.useFilter(dataset, replaceMissing);

            // Verify and print attribute types
            System.out.println("Attributes in the cleaned dataset:");
            for (int i = 0; i < filteredDataset.numAttributes(); i++) {
                String attributeName = filteredDataset.attribute(i).name();
                String attributeType = "";
                switch (filteredDataset.attribute(i).type()) {
                    case 0: // Nominal
                        attributeType = "Nominal";
                        break;
                    case 1: // Numeric
                        attributeType = "Numeric";
                        break;
                    case 2: // String
                        attributeType = "String";
                        break;
                    case 3: // Date
                        attributeType = "Date";
                        break;
                    default:
                        attributeType = "Unknown";
                }
                System.out.println("Attribute " + i + ": " + attributeName + " - Type: " + attributeType);
            }

            // Save the cleaned dataset
            ArffSaver saver = new ArffSaver();
            saver.setInstances(filteredDataset);
            saver.setFile(new File("/Users/olawale/Desktop/Devs/EVENUE/ML/cleaned_user_behaviour_new.arff"));
            saver.writeBatch();

            System.out.println("Data processing completed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
