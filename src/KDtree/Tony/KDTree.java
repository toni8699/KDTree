package KDtree.Tony;

import java.util.ArrayList;
import java.util.Iterator;
public class KDTree implements Iterable<Datum>{

	KDNode 		rootNode;
	int    		k; 
	int			numLeaves;
	
	// constructor

	public KDTree(ArrayList<Datum> datalist) throws Exception {

		Datum[]  dataListArray  = new Datum[ datalist.size() ]; 

		if (datalist.size() == 0) {
			throw new Exception("Trying to create a KD tree with no data");
		}
		else
			this.k = datalist.get(0).x.length;

		int ct=0;
		for (Datum d :  datalist) {
			dataListArray[ct] = datalist.get(ct);
			ct++;
		}
		
	//   Construct a KDNode that is the root node of the KDTree.

		rootNode = new KDNode(dataListArray);
	}
	
	//   KDTree methods
	
	public Datum nearestPoint(Datum queryPoint) {
		return rootNode.nearestPointInNode(queryPoint);
	}
	

	public int height() {
		return this.rootNode.height();	
	}

	public int countNodes() {
		return this.rootNode.countNodes();	
	}
	
	public int size() {
		return this.numLeaves;	
	}

	//-------------------  helper methods for KDTree   ------------------------------

	public static long distSquared(Datum d1, Datum d2) {

		long result = 0;
		for (int dim = 0; dim < d1.x.length; dim++) {
			result +=  (d1.x[dim] - d2.x[dim])*((long) (d1.x[dim] - d2.x[dim]));
		}
		return result;
	}

	public double meanDepth(){
		int[] sumdepths_numLeaves =  this.rootNode.sumDepths_numLeaves();
		return 1.0 * sumdepths_numLeaves[0] / sumdepths_numLeaves[1];
	}

	
	class KDNode { 

		boolean leaf;
		Datum leafDatum;           //  only stores Datum if this is a leaf
		
		//  the next two variables are only defined if node is not a leaf

		int splitDim;      // the dimension we will split on
		int splitValue;    // datum is in low if value in splitDim <= splitValue, and high if value in splitDim > splitValue

		KDNode lowChild, highChild;   //  the low and high child of a particular node (null if leaf)
		  //  You may think of them as "left" and "right" instead of "low" and "high", respectively

		KDNode(Datum[] datalist) throws Exception{
		//	System.out.println(Arrays.toString(datalist));

			/*
			 *  This method takes in an array of Datum and returns 
			 *  the calling KDNode object as the root of a sub-tree containing
			 *  the above fields.
			 */

			int max;
			max =SplitDim(datalist);
			if ( datalist.length ==1 || max ==0){
             //   System.out.println("base case is1 " +(Arrays.toString(datalist)));
			 //       System.out.println("base case is" +(Arrays.toString(datalist)));
				leaf= true;
				leafDatum=datalist[0];
				lowChild=null;
				highChild=null;
				numLeaves++;
			}
			else {
				int count1 = 0, count2 = 0;
				for (int c = 0; c < datalist.length; c++) {
					if (datalist[c].x[splitDim] > splitValue) {
						count1++;
					} else
					{
						count2++;
					}
				}
				Datum [] High= new Datum[count1];
				Datum [] Low = new Datum[count2];
				int h =0, l=0;

				for (int c = 0; c < datalist.length; c++) {
					if (datalist[c].x[splitDim] > splitValue) {
						High[h]=datalist[c];
						h++;
					} else
					    {
						Low[l]=datalist[c];
						l++;
					}
				}
			//	System.out.println("low child is " + Arrays.toString(Low));
             //   System.out.println("high child is " + Arrays.toString(High));

				/*System.out.println("High = " + count1);
				System.out.println("low = "+ count2);
				System.out.println(Arrays.toString(High));
				System.out.println(Arrays.toString(Low)); */

				highChild = new KDNode(High);
                lowChild =   new KDNode(Low);

			}




		}

		public Datum nearestPointInNode(Datum queryPoint) {
			Datum nearestPoint, nearestPoint_otherSide;
			long dis;


			if (this.leaf){
				nearestPoint= this.leafDatum;
				}
			else{
				dis = Math.abs((queryPoint.x[splitDim] -this.splitValue)*(queryPoint.x[splitDim] -this.splitValue));
				if (queryPoint.x[splitDim]>this.splitValue)
				{
				    nearestPoint =this.highChild.nearestPointInNode(queryPoint);
				if ( distSquared(nearestPoint,queryPoint)<dis)
				{
					return nearestPoint;
				}
				else
				    {
						nearestPoint_otherSide = this.lowChild.nearestPointInNode(queryPoint);
						if ( distSquared(nearestPoint,queryPoint)> distSquared(nearestPoint_otherSide,queryPoint))
						{
							return nearestPoint_otherSide;
						}else{
							return nearestPoint;
						}
				}
				}
				else{
					nearestPoint=this.lowChild.nearestPointInNode(queryPoint);
					if ( distSquared(nearestPoint,queryPoint)<dis){
						return nearestPoint;}
					else{
						nearestPoint_otherSide= this.highChild.nearestPointInNode(queryPoint);
						if ( distSquared(nearestPoint,queryPoint)> distSquared(nearestPoint_otherSide,queryPoint)){
							return nearestPoint_otherSide;
						}else{
							return nearestPoint;
						}
					}
				}

				}
			return nearestPoint;
		}
		// -----------------  KDNode helper methods -------------------
        private int SplitDim(Datum[] array){
			int[] curmin = new int[k];
			int[] curmax = new int[k];
			int[] range = new int[k];
			for (int i = 0; i < k; i++) {
				curmin[i] = array[0].x[i];
				curmax[i] = array[0].x[i];
			}
			//	System.out.println("curmax[] = "+ Arrays.toString(curmax));
			//	System.out.println("curmmin[] = "+ Arrays.toString(curmin));

			for (Datum d : array) {
				for (int i = 0; i < k; i++) {
					curmin[i] = min(curmin[i], d.x[i]);
					curmax[i] = max(curmax[i], d.x[i]);
				}
			}
			//    System.out.println("curmax[] = "+ Arrays.toString(curmax));
			//   System.out.println("curmmin[] = "+ Arrays.toString(curmin));


			for (int i = 0; i < k; i++) {
				range[i] = curmax[i] - curmin[i];
			}
			//	System.out.println("range is "+ Arrays.toString(range));
			int max = range[0];
			for (int i = 0; i < k; i++) {
				if (max <= range[i]) {
					splitDim=i;
					max = range[i];
				}

				if ( curmax[splitDim]<=0 && curmax[splitDim]<=0 && max==1 ){
					splitValue = (curmax[splitDim]+curmin[splitDim])/2 -1;
				}else{
					splitValue =  ((curmax[splitDim]+curmin[splitDim])/2);
				}}
        return max;
		}
        private boolean checkEquals(Datum[] array){
            if (array.length ==0){
                return true;
            }
            else{
                Datum first = array[0];
                for ( Datum d: array){
                    if (!(d.equals(first))){
                        return false;
                    }
                }
                return true;
            }
        }


		private int min(int a,int b){
			if ( a==b)
				return a;
			else if (a<b){
				return a;}
			else
				return b;
		}
		private int max(int a,int b){
			if ( a>b){
				return a;
			}
			else if (a==b){
				return a;

			}else{
				return b;
			}
		}
		public int height() {
			if (this.leaf) 	
				return 0;
			else {
				return 1 + Math.max( this.lowChild.height(), this.highChild.height());
			}
		}

		public int countNodes() {
			if (this.leaf)
				return 1;
			else
				return 1 + this.lowChild.countNodes() + this.highChild.countNodes();
		}
		
		/*  
		 * Returns a 2D array of ints.  The first element is the sum of the depths of leaves
		 * of the subtree rooted at this KDNode.   The second element is the number of leaves
		 * this subtree.    Hence,  I call the variables  sumDepth_size_*  where sumDepth refers
		 * to element 0 and size refers to element 1.
		 */
				
		public int[] sumDepths_numLeaves(){
			int[] sumDepths_numLeaves_low, sumDepths_numLeaves_high;
			int[] return_sumDepths_numLeaves = new int[2];
			
			/*     
			 *  The sum of the depths of the leaves is the sum of the depth of the leaves of the subtrees, 
			 *  plus the number of leaves (size) since each leaf defines a path and the depth of each leaf 
			 *  is one greater than the depth of each leaf in the subtree.
			 */
			
			if (this.leaf) {  // base case
				return_sumDepths_numLeaves[0] = 0;
				return_sumDepths_numLeaves[1] = 1;
			}
			else {
				sumDepths_numLeaves_low  = this.lowChild.sumDepths_numLeaves();
				sumDepths_numLeaves_high = this.highChild.sumDepths_numLeaves();
				return_sumDepths_numLeaves[0] = sumDepths_numLeaves_low[0] + sumDepths_numLeaves_high[0] + sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
				return_sumDepths_numLeaves[1] = sumDepths_numLeaves_low[1] + sumDepths_numLeaves_high[1];
			}	
			return return_sumDepths_numLeaves;
		}
		
	}

	public Iterator<Datum> iterator() {
		return new KDTreeIterator();
	}
	
	private class KDTreeIterator implements Iterator<Datum> {
       private ArrayList<Datum> datumList;
		int index = 0;

		// inorder(){
		// while rootnode!=null

		// }
		// if leaf then populate datumList
		//else recursively call leftchild and right child


		// hasNext check if index  is less than datumlist.length
		// next can return datumlIts[index++]
		private void inorder(KDNode node){
			if (node== null){
				return;
			}
			if ( node.leaf){
			datumList.add(node.leafDatum);}
			inorder(node.lowChild);
			inorder(node.highChild);
		}


        public KDTreeIterator(){
			this.datumList= new ArrayList<>();
			inorder(rootNode);
	        }

		@Override
		public boolean hasNext() {
			return index<datumList.size();
		}

		@Override
		public Datum next() {
			return datumList.get(index++);
		}
	}



}

