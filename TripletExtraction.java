/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package TripletExtraction;



import java.util.List;
import edu.stanford.nlp.trees.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author sushant
 */
public class TripletExtraction {


    public Map<String,Tree> KernelExtraction(Tree tree)
    {
        Map<String,Tree> kernel = new HashMap<String,Tree>();
        Map<String,List<Tree>> subKernel = new HashMap<String,List<Tree>>();

        tree = tree.getChild(0);

        List<Tree> children = tree.getChildrenAsList();

        if(children.get(0).label().value().equals("NP")) //sentence
        {
            Tree NP_subtree = children.get(0);
            Tree VP_subtree = null;

            for(int i=1;i<children.size();i++)
            {
                if(children.get(i).label().value().equals("VP"))
                {
                    VP_subtree = children.get(i);
                    break;
                }
            }

            Object noun[] = extractSubject(NP_subtree);
            Object verb[] = extractVerb(VP_subtree);
            Object object[] = extractObject(VP_subtree);

            kernel.put("noun",(Tree)noun[0]);
            kernel.put("verb",(Tree)verb[0]);
            kernel.put("object",(Tree)object[0]);

            subKernel.put("noun-attr",(List<Tree>)noun[1]);
            subKernel.put("verb-attr",(List<Tree>)verb[1]);
            subKernel.put("object-attr",(List<Tree>)object[1]);

        }else if(children.get(0).label().value().equals("VP")) //fragment
        {
            Tree VP_subtree = children.get(0);

            Object verb[] = extractVerb(VP_subtree);
            Object object[] = extractObject(VP_subtree);

            kernel.put("verb",(Tree)verb[0]);
            kernel.put("object",(Tree)object[0]);

            subKernel.put("verb-attr",(List<Tree>)verb[1]);
            subKernel.put("object-attr",(List<Tree>)object[1]);
        }

        System.out.println(kernel+"\n"+subKernel);
        return kernel;

    }

    public Object[] extractSubject(Tree NP_subtree)
    {
        for(Tree leaf:NP_subtree.getLeaves())
        {
            leaf = leaf.parent(NP_subtree);
            if(leaf.label().value().startsWith("N"))
            {
                Tree parent = leaf.parent(NP_subtree);
                List<Tree> siblings = parent.getChildrenAsList();
                List<Tree> attributes = extractAttributes(leaf,siblings);
                return new Object[]{leaf,attributes};
            }
        }

        return null;
    }

    public Object[] extractVerb(Tree VP_subtree)
    {
        Tree vp = getDeepestVerb(VP_subtree);

        Tree parent = vp.parent(VP_subtree);
        List<Tree> siblings = parent.getChildrenAsList();
        List<Tree> attributes = extractAttributes(vp,siblings);
        return new Object[]{vp,attributes};
    }

    public Object[] extractObject(Tree VP_subtree)
    {
        boolean doItAgain = false;
        List<Tree> siblings = new ArrayList<Tree>();

        do{
            doItAgain = false;
            for(Tree child:VP_subtree.getChildrenAsList())
            {
                if(child.label().value().equals("NP") ||
                        child.label().value().equals("PP")||
                        child.label().value().equals("JJ"))
                {
                    siblings.add(child);
                }else if(child.label().value().equals("VP") || child.label().value().equals("ADJP"))
                {
                    VP_subtree=child;
                    doItAgain= true;
                    break;
                }
            }
        }while(doItAgain);

        for(Tree value:siblings)
        {
            String val = value.label().value();
            if(val.equals("NP") || val.equals("PP"))
            {
                for(Tree leaf:value.getLeaves())
                {
                    leaf = leaf.parent(value);
                    if(leaf.label().value().startsWith("N"))
                    {
                        Tree parent = leaf.parent(VP_subtree);
                        List<Tree> sibling = parent.getChildrenAsList();
                        List<Tree> attributes = extractAttributes(leaf,sibling);
                        return new Object[]{leaf,attributes};
                    }
                }
            }else
            {
                for(Tree leaf:value.getLeaves())
                {
                    leaf = leaf.parent(value);
                    if(leaf.label().value().startsWith("J"))
                    {
                        Tree parent = leaf.parent(VP_subtree);
                        List<Tree> sibling = parent.getChildrenAsList();
                        List<Tree> attributes = extractAttributes(leaf,sibling);
                        return new Object[]{leaf,attributes};
                    }
                }
            }
        }

        return null;
    }

    public Tree getDeepestVerb(Tree vp)
    {
        int max = -1;
        Tree verb = null;
        List<Tree> leaves = vp.getLeaves();
        for(Tree leaf:leaves)
        {
            leaf = leaf.parent(vp);

            if(leaf.label().value().startsWith("V"))
            {
                int i = vp.depth(leaf);
                if(i > max)
                {
                    max = vp.depth(leaf);
                    verb = leaf;
                }
            }
        }

        return verb;
    }

    public List<Tree> extractAttributes(Tree word, List<Tree> siblings)
    {
        int index = siblings.indexOf(word);
        siblings.remove(index);

        
        List<Tree> result = new ArrayList<Tree>();

        if(word.label().value().startsWith("J")) //Adjective
        {
            for(Tree sib:siblings)
            {
                if(sib.label().value().equals("RB"))
                {
                    result.add(sib);
                }
            }
        }else
        {
            if(word.label().value().startsWith("N"))
            {
                for(Tree sib:siblings)
                {
                    if(sib.label().value().equals("DT")||
                            sib.label().value().equals("PRP$") ||
                            sib.label().value().equals("POS") ||
                            sib.label().value().equals("JJ") ||
                            sib.label().value().equals("CD") ||
                            sib.label().value().equals("ADJP") ||
                            sib.label().value().equals("QP") ||
                            sib.label().value().equals("NP"))
                    {
                        result.add(sib);
                    }
                }
            }else
            {
                if(word.label().value().startsWith("V"))
                {
                    for(Tree sib:siblings)
                    {
                        if(sib.label().value().equals("ADJP"))
                        {
                            result.add(sib);
                        }
                    }
                }
            }
        }

        return result;
    }

}
