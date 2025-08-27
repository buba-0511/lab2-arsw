package edu.eci.arsw.primefinder;

import java.util.LinkedList;
import java.util.List;

public class PrimeFinderThread extends Thread{

	
	int a,b;
    private PauseController pauseController;
	private List<Integer> primes;
	
	public PrimeFinderThread(int a, int b, PauseController pauseController) {
		super();
        this.primes = new LinkedList<>();
        this.pauseController = pauseController;
		this.a = a;
		this.b = b;
	}

        @Override
	public void run(){
        for (int i= a;i < b;i++){

            pauseController.checkPause();

            if (isPrime(i)){
                primes.add(i);
                System.out.println(i);
            }
        }
	}
	
	boolean isPrime(int n) {
	    boolean ans;
            if (n > 2) { 
                ans = n%2 != 0;
                for(int i = 3;ans && i*i <= n; i+=2 ) {
                    ans = n % i != 0;
                }
            } else {
                ans = n == 2;
            }
	    return ans;
	}

	public List<Integer> getPrimes() {
		return primes;
	}
}
