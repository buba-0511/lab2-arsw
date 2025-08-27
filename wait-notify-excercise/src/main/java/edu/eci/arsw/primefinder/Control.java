/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.primefinder;

/**
 *
 */
public class Control extends Thread {
    
    private final static int NTHREADS = 3;
    private final static int MAXVALUE = 30000000;
    private final static int TMILISECONDS = 5000;

    private final int NDATA = MAXVALUE / NTHREADS;

    private PrimeFinderThread pft[];
    private PauseController pauseController;
    
    private Control() {
        super();
        this.pft = new  PrimeFinderThread[NTHREADS];
        this.pauseController = new PauseController();

        int i;
        for(i = 0;i < NTHREADS - 1; i++) {
            PrimeFinderThread elem = new PrimeFinderThread(i*NDATA, (i+1)*NDATA, pauseController);
            pft[i] = elem;
        }
        pft[i] = new PrimeFinderThread(i*NDATA, MAXVALUE + 1, pauseController);
    }
    
    public static Control newControl() {
        return new Control();
    }

    @Override
    public void run() {
        for(int i = 0;i < NTHREADS;i++ ) {
            pft[i].start();
        }

        try {
            while (true) {
                Thread.sleep(TMILISECONDS);

                pauseController.pause();

                int total = 0;
                boolean finished = true;
                for (PrimeFinderThread i : pft) {
                    total += i.getPrimes().size();
                    if (i.isAlive()) {
                        finished = false;
                    }
                }


                System.out.println("Pausa global. Primos encontrados: " + total);

                if (finished) {
                    System.out.println("Todos los hilos terminaron. Fin del programa.");
                    break;
                }

                System.out.println("Presiona ENTER para continuar...");

                new java.io.BufferedReader(
                    new java.io.InputStreamReader(System.in)
                ).readLine();

                pauseController.resume();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
