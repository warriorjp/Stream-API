// Java implementation of recursive Binary Search
class BinarySearch {

	int binarySearch(int arr[], int low, int high, int x)
	{
		if (high >= low) {
			int mid = low + (high - low) / 2;
			
			if (arr[mid] == x) {
				return mid;
			}
				
			if (arr[mid] > x) {
				return binarySearch(arr, low, mid - 1, x);
			}

			return binarySearch(arr, mid + 1, high, x);
		}

		return -1;
	}
	public static void main(String args[])
	{
		BinarySearch ob = new BinarySearch();
		int arr[] = { 2, 3, 4, 10, 40 };  //array
		int n = arr.length;              //length
		int x = 	2;                               //element
		int result = ob.binarySearch(arr, 0, n - 1, x);
		if (result == -1)
			System.out.println("Element not present");
		else
			System.out.println("Element found at index "
							+ result);
	}
}
