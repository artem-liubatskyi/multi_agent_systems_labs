package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LabyrinthCell {
    public List<LabyrinthCellDescriptor> Descriptors;
    public LabyrinthCell(LabyrinthCellDescriptor[] descriptors){
        Descriptors = new ArrayList<>(Arrays.asList(descriptors));
    }
}
