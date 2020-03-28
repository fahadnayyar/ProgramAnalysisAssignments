package CallGraphCreation;

import java.util.Scanner;

//Test Case 1:

//public class Sample_test
//{		
//	int foofoo(int x)
//	{
//		return 0;
//	}
//	int func (int x)
//	{
//	  int t = x;
//	  System.out.println("hi");
//	  t=-x;
//	  int s=-t+x;
//	  t = 100;
//	  if(t>6)
//	  {
//		  t=0;
//	  }
//	  else
//	  {
//		  t=-100;
//	  }
//	  x=x+1;
//	  return t;
//	}
//	
//	int foo (int p)
//	{
//	  int a = p;
//	  a=func(2);
//	  int l = a  + func(-1);//
//	  int k=foofoo(4);
//	  int g = 1+ func(l);
//	  return g;
//	}
//	
//	public static void main (String[] args)
//	{
//	    int play = Integer.parseInt(args[0]);
//	    Sample_test s = new Sample_test();
//	    int x=0;
//	    int y = 0;
//	    if(play > 0)
//	      x = s.foo(play);
//	    else
//	    {
//	      y = s.foo(4);
//	      System.out.println(y);
//	    }
//	    System.out.println(x);
//	}
//}

//TestCase 1

//public class Sample_test {
//	static int bar(int x) {
//		x = x * 5;
//		return x;
//	}
//
//	static int foo(int x, int y) {
//		int z = 5;
//		int res;
//		int p = bar(z);
//		int q = bar(x);
//		res = p * q;
//		return res;
//	}
//
//	public static void main(String[] args) {
//		Scanner sc = new Scanner(System.in);
////		int e = Integer.parseInt(args[1]);
//		int n = sc.nextInt();
//		int k = sc.nextInt();
//		int a, b, c, d, e;
//		a = foo(n, k);
//		b = foo(n, 1);
//		c = foo(1, k);
//		d = foo(1, 10);
//		e = bar(n);
//		System.out.println(a);
//		System.out.println(b);
//		System.out.println(c);
//		System.out.println(d);
//		System.out.println(e);
//	}
//}

public class Sample_test {
	static int bar1(int x) {
		x = x * 5;
		return x;
	}

	static int bar2(int x) {
		Scanner sc1 = new Scanner(System.in);
		int y = sc1.nextInt();
		x = x * 5;
		return y;
	}

	static int foo(int x, int y) {
		int z = 5;
		int res, p;
		if (x > y) {
			p = bar1(z);
		} else {
			p = bar2(5);
		}
		res = p;
		return res;
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int n = sc.nextInt();
		int k = sc.nextInt();
		int a, b, c, d, e;
		a = foo(n, k); // a is tainted
		b = foo(n, 1); // b is tainted
		c = foo(1, k); // c is tainted
		d = bar2(1); // d is tainted
		e = bar1(1); // e is untainted
		System.out.println(a);
		System.out.println(b);
		System.out.println(c);
		System.out.println(d);
		System.out.println(e);
	}
}