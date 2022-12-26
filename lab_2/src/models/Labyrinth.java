package models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Labyrinth {
    private Hero Hero = new Hero();
    private LabyrinthCell[][] Rooms = new LabyrinthCell[4][4];

    public Labyrinth(){
        Rooms[0][0] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Hero});
        Rooms[0][1] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Breeze});
        Rooms[0][2] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Pit});
        Rooms[0][3] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Breeze});
        Rooms[1][0] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Stench});
        Rooms[1][1] = new LabyrinthCell(new LabyrinthCellDescriptor[] {});
        Rooms[1][2] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Breeze});
        Rooms[1][3] = new LabyrinthCell(new LabyrinthCellDescriptor[] {});
        Rooms[2][0] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Wumpus});
        Rooms[2][1] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Stench, LabyrinthCellDescriptor.Glitch,LabyrinthCellDescriptor.Breeze});
        Rooms[2][2] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Pit});
        Rooms[2][3] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Breeze});
        Rooms[3][0] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Stench});
        Rooms[3][1] = new LabyrinthCell(new LabyrinthCellDescriptor[] {});
        Rooms[3][2] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Breeze});
        Rooms[3][3] = new LabyrinthCell(new LabyrinthCellDescriptor[] {LabyrinthCellDescriptor.Pit});
    }

        private int[] GetHeroCoords(){
            for(int i =0; i<4;i++){
                for(int j =0; j<4;j++){
                    if(Arrays.stream(Rooms[i][j].Descriptors).allMatch(x->x ==LabyrinthCellDescriptor.Hero)){
                        return new int[]{i,j};
                    }
                }
            }
            return new int[]{};
        }

    public List<LabyrinthCellDescriptor> GetHeroRoomState(){
        var heroCoords =GetHeroCoords();

       return Arrays.stream(Rooms[heroCoords[0]][heroCoords[1]].Descriptors).filter(x->x!=LabyrinthCellDescriptor.Hero).toList();
    }

    public void UpdateHeroDirection(Direction direction){
        Hero.Direction = direction;
    }

    public LabyrinthCell MoveHero(){
        var heroCoords = GetHeroCoords();
        int[] updatedCoords = new int []{heroCoords[0], heroCoords[1]};
        switch (Hero.Direction){
            case Up:
                updatedCoords[1]+=1;
                break;

            case Down:
                updatedCoords[1]-=1;
                break;

            case Left:
                updatedCoords[0]-=1;
                break;

            case Right:
                updatedCoords[0]+=1;
                break;
        }
        if( updatedCoords[0]<0 ||  updatedCoords[0]>4 ||  updatedCoords[0]<0 ||  updatedCoords[0]>4){
            return null;
        }

        Rooms[updatedCoords[0]][updatedCoords[1]].Descriptors =(LabyrinthCellDescriptor[]) Stream.concat(Arrays.stream(Rooms[updatedCoords[0]][updatedCoords[1]].Descriptors),
                Arrays.stream(new LabyrinthCellDescriptor[]{LabyrinthCellDescriptor.Hero})).toArray();

        Rooms[heroCoords[0]][heroCoords[1]].Descriptors =(LabyrinthCellDescriptor[]) Arrays.stream(Rooms[heroCoords[0]][heroCoords[1]].Descriptors).filter(x->x!=LabyrinthCellDescriptor.Hero).toArray();

        return Rooms[updatedCoords[0]][updatedCoords[1]];
    }

    public boolean IsHeroDead(){
        var heroCoords =GetHeroCoords();

        return Arrays.stream(Rooms[heroCoords[0]][heroCoords[1]].Descriptors).anyMatch(x -> x != LabyrinthCellDescriptor.Hero) &&
                Arrays.stream(Rooms[heroCoords[0]][heroCoords[1]].Descriptors).anyMatch(x -> x != LabyrinthCellDescriptor.Wumpus);

    }

    public boolean IsWin(){
        var heroCoords =GetHeroCoords();

        return Arrays.stream(Rooms[heroCoords[0]][heroCoords[1]].Descriptors).anyMatch(x -> x != LabyrinthCellDescriptor.Hero) &&
                Arrays.stream(Rooms[heroCoords[0]][heroCoords[1]].Descriptors).anyMatch(x -> x != LabyrinthCellDescriptor.Glitch);

    }
}
