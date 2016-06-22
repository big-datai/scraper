package com.utils.aws;

import java.util.ArrayList;

class A {

	public static void main(String[] str) {

		//
		//
		//
		int[][] arr = new int[][] { { 1, 1, 1 }, { 1, 1, 0 }, { 1, 1, 1 } };

		System.out.println(foo(arr, 0, 0, 3, 3));
	}

	public static int foo(int[][] graph, int i, int j, int n, int m) {
		int right = 0, down = 0;
		if (i == n - 1 && j == m - 1) {
			return 1;
		}
		if (i + 1 < n && graph[i + 1][j] == 1) {
			right = foo(graph, i + 1, j, n, m);
		}
		if (j + 1 < m && graph[i][j + 1] == 1) {
			down = foo(graph, i, j + 1, n, m);
		}
		return right + down;

	}

}