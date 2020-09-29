package wzy.jsp.simplest.demo;

import static org.junit.Assert.assertTrue;

import org.javatuples.Pair;
import org.junit.Test;
import wzy.jsp.simplest.demo.component.DateTimeConverter;
import wzy.jsp.simplest.demo.component.VariableConverter;
import wzy.jsp.simplest.demo.domain.algorithm.GAAlgorithmWithDisjunctiveGraph;
import wzy.jsp.simplest.demo.domain.algorithm.represent.DisjunctiveGraphModel;
import wzy.jsp.simplest.demo.domain.algorithm.represent.GAParametersWithDisjunctiveGraph;
import wzy.jsp.simplest.demo.domain.algorithm.represent.NodeInDG;
import wzy.jsp.simplest.demo.domain.communication.Solution;
import wzy.jsp.simplest.demo.generator.TestSolutionGenerator;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    //@Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    //@Test
    public void testShuffleTaskPermutation(){
        VariableConverter variableConverter=new VariableConverter(new DateTimeConverter());
        Solution testSolution=new TestSolutionGenerator().GenerateTestSolution2();
        DisjunctiveGraphModel disjunctiveGraphModel=variableConverter.getDisjunctiveGraphFromSolution(testSolution,true);
        for(int i=0;i<10;i++){
            Map<String, List<NodeInDG>> taskPermutation=disjunctiveGraphModel.nodesPerMachine;
            taskPermutation=new GAAlgorithmWithDisjunctiveGraph(new GAParametersWithDisjunctiveGraph(disjunctiveGraphModel,testSolution),
                    new VariableConverter(new DateTimeConverter()),
                    null,
                    null).shuffleTaskPermutation(taskPermutation);
            for(Map.Entry<String, List<NodeInDG>> v : taskPermutation.entrySet()){
                String machineName=v.getKey();
                List<NodeInDG> nodes=v.getValue();

                String s=machineName+" : ";
                for(NodeInDG node : nodes){
                    s+="task_"+node.taskIndexs.getValue0()+"_"+node.taskIndexs.getValue1()+", ";
                }
                System.out.println(s);
            }

            System.out.println("-----------------------------------------------");
        }
    }

    //@Test
    public void RandomTest(){
        int permutationSize=8;
        for(int i=0;i<50;i++){
            int crossIndex1=new Random().nextInt(permutationSize);
            int crossIndex2=Integer.parseInt(new DecimalFormat("0").format(new Random().nextDouble() * (permutationSize-1 - crossIndex1) + crossIndex1));
            System.out.println("loop "+i);
            System.out.println("crossIndex1 : "+crossIndex1+", "+"crossIndex2 : "+crossIndex2);
        }

    }

    //@Test
    public void testGACross(){
        VariableConverter variableConverter=new VariableConverter(new DateTimeConverter());
        Solution testSolution=new TestSolutionGenerator().GenerateTestSolution2();
        DisjunctiveGraphModel disjunctiveGraphModel=variableConverter.getDisjunctiveGraphFromSolution(testSolution,true);
        GAAlgorithmWithDisjunctiveGraph ga=new GAAlgorithmWithDisjunctiveGraph(new GAParametersWithDisjunctiveGraph(disjunctiveGraphModel,testSolution),
                new VariableConverter(new DateTimeConverter()),
                null,
                null);

        Map<String, List<NodeInDG>> parentPermutation1=new HashMap<>();
        parentPermutation1.put("A",new ArrayList<>());
        NodeInDG node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(1,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(3,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(4,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(5,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(6,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(7,0);
        parentPermutation1.get("A").add(node);

        parentPermutation1.put("B",new ArrayList<>());
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(1,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(3,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(4,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(5,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(6,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(7,1);
        parentPermutation1.get("B").add(node);

        GAAlgorithmWithDisjunctiveGraph.Chromosome chromosome1=new GAAlgorithmWithDisjunctiveGraph.Chromosome(parentPermutation1);

        System.out.println("Task Permutation of Parent 1 :");
        for(Map.Entry<String, List<NodeInDG>> v : parentPermutation1.entrySet()){
            String s=v.getKey()+" : ";
            for(NodeInDG nodeInDG : v.getValue()){
                s+=nodeInDG.taskIndexs.getValue0()+" , ";//+"_"+nodeInDG.taskIndexs.getValue1()+", ";
            }
            System.out.println(s);
        }


        Map<String, List<NodeInDG>> parentPermutation2=new HashMap<>();
        parentPermutation2.put("A",new ArrayList<>());
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(7,0);
        parentPermutation2.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(6,0);
        parentPermutation2.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(5,0);
        parentPermutation2.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(4,0);
        parentPermutation2.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(3,0);
        parentPermutation2.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,0);
        parentPermutation2.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(1,0);
        parentPermutation2.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,0);
        parentPermutation2.get("A").add(node);

        parentPermutation2.put("B",new ArrayList<>());
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(7,1);
        parentPermutation2.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(6,1);
        parentPermutation2.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(5,1);
        parentPermutation2.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(4,1);
        parentPermutation2.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(3,1);
        parentPermutation2.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,1);
        parentPermutation2.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(1,1);
        parentPermutation2.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,1);
        parentPermutation2.get("B").add(node);
        GAAlgorithmWithDisjunctiveGraph.Chromosome chromosome2=new GAAlgorithmWithDisjunctiveGraph.Chromosome(parentPermutation2);
        System.out.println("Task Permutation of Parent 2 :");
        for(Map.Entry<String, List<NodeInDG>> v : parentPermutation2.entrySet()){
            String s=v.getKey()+" : ";
            for(NodeInDG nodeInDG : v.getValue()){
                s+=nodeInDG.taskIndexs.getValue0()+" , ";//+"_"+nodeInDG.taskIndexs.getValue1()+", ";
            }
            System.out.println(s);
        }

//        GAAlgorithmWithDisjunctiveGraph.Chromosome childChromosome=ga.getUsxxCrossChild(chromosome1,chromosome2);
//        System.out.println("Task Permutation of Child :");
//        for(Map.Entry<String, List<NodeInDG>> v : childChromosome.getTaskPermutation().entrySet()){
//            String s=v.getKey()+" : ";
//            for(NodeInDG nodeInDG : v.getValue()){
//                s+=nodeInDG.taskIndexs.getValue0()+" , ";//+"_"+nodeInDG.taskIndexs.getValue1()+", ";
//            }
//            System.out.println(s);
//        }

        GAAlgorithmWithDisjunctiveGraph.Chromosome childChromosome2=ga.getUsxxCrossChild(chromosome2,chromosome1);
        System.out.println("Task Permutation of Child :");
        for(Map.Entry<String, List<NodeInDG>> v : childChromosome2.getTaskPermutation().entrySet()){
            String s=v.getKey()+" : ";
            for(NodeInDG nodeInDG : v.getValue()){
                s+=nodeInDG.taskIndexs.getValue0()+" , ";//+"_"+nodeInDG.taskIndexs.getValue1()+", ";
            }
            System.out.println(s);
        }
    }

    //@Test
    public void TestMutate(){
        VariableConverter variableConverter=new VariableConverter(new DateTimeConverter());
        Solution testSolution=new TestSolutionGenerator().GenerateTestSolution2();
        DisjunctiveGraphModel disjunctiveGraphModel=variableConverter.getDisjunctiveGraphFromSolution(testSolution,true);
        GAAlgorithmWithDisjunctiveGraph ga=new GAAlgorithmWithDisjunctiveGraph(new GAParametersWithDisjunctiveGraph(disjunctiveGraphModel,testSolution),
                new VariableConverter(new DateTimeConverter()),
                null,
                null);

        Map<String, List<NodeInDG>> parentPermutation1=new HashMap<>();
        parentPermutation1.put("A",new ArrayList<>());
        NodeInDG node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(1,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(3,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(4,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(5,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(6,0);
        parentPermutation1.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(7,0);
        parentPermutation1.get("A").add(node);

        parentPermutation1.put("B",new ArrayList<>());
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(1,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(3,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(4,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(5,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(6,1);
        parentPermutation1.get("B").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(7,1);
        parentPermutation1.get("B").add(node);

        GAAlgorithmWithDisjunctiveGraph.Chromosome chromosome1=new GAAlgorithmWithDisjunctiveGraph.Chromosome(parentPermutation1);

        System.out.println("Task Permutation of Parent 1 :");
        for(Map.Entry<String, List<NodeInDG>> v : parentPermutation1.entrySet()){
            String s=v.getKey()+" : ";
            for(NodeInDG nodeInDG : v.getValue()){
                s+=nodeInDG.taskIndexs.getValue0()+" , ";//+"_"+nodeInDG.taskIndexs.getValue1()+", ";
            }
            System.out.println(s);
        }

        ga.MutateOneChromosome(chromosome1);
        System.out.println("Mutate Permutation :");
        for(Map.Entry<String, List<NodeInDG>> v : parentPermutation1.entrySet()){
            String s=v.getKey()+" : ";
            for(NodeInDG nodeInDG : v.getValue()){
                s+=nodeInDG.taskIndexs.getValue0()+" , ";//+"_"+nodeInDG.taskIndexs.getValue1()+", ";
            }
            System.out.println(s);
        }

    }

    @Test
    public void TestRepair(){
        VariableConverter variableConverter=new VariableConverter(new DateTimeConverter());
        Solution testSolution=new TestSolutionGenerator().GenerateTestSolution2();
        DisjunctiveGraphModel disjunctiveGraphModel=variableConverter.getDisjunctiveGraphFromSolution(testSolution,true);
        GAAlgorithmWithDisjunctiveGraph ga=new GAAlgorithmWithDisjunctiveGraph(new GAParametersWithDisjunctiveGraph(disjunctiveGraphModel,testSolution),
                new VariableConverter(new DateTimeConverter()),
                null,
                null);

        Map<String, List<NodeInDG>> parentPermutation=new HashMap<>();
        parentPermutation.put("A",new ArrayList<>());
        NodeInDG node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,1);
        parentPermutation.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,0);
        parentPermutation.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,1);
        parentPermutation.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,2);
        parentPermutation.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(0,3);
        parentPermutation.get("A").add(node);
        node=new NodeInDG();
        node.taskIndexs=new Pair<Integer, Integer>(2,0);
        parentPermutation.get("A").add(node);

        System.out.println("Before repair:");
        for(Map.Entry<String, List<NodeInDG>> v : parentPermutation.entrySet()){
            String s=v.getKey()+" : ";
            for(NodeInDG nodeInDG : v.getValue()){
                s+=nodeInDG.taskIndexs.getValue0()+"_"+nodeInDG.taskIndexs.getValue1()+", ";
            }
            System.out.println(s);
        }

        GAAlgorithmWithDisjunctiveGraph.Chromosome chromosome1=new GAAlgorithmWithDisjunctiveGraph.Chromosome(parentPermutation);
        chromosome1=ga.LocalRepair(chromosome1);

        parentPermutation=chromosome1.getTaskPermutation();
        System.out.println("After repair:");
        for(Map.Entry<String, List<NodeInDG>> v : parentPermutation.entrySet()){
            String s=v.getKey()+" : ";
            for(NodeInDG nodeInDG : v.getValue()){
                s+=nodeInDG.taskIndexs.getValue0()+"_"+nodeInDG.taskIndexs.getValue1()+", ";
            }
            System.out.println(s);
        }



    }


}
