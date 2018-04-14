import java.util.Random; 

class CoinFlip implements Runnable {
	private int heads; // heads per thread
	private int tosses;
	Random rand;

	public void run() {
		for(int i = 0; i < tosses; i++) { 
			int randNum = rand.nextInt(2);

			if(randNum == 0) {
				heads++;
			}	
		}
        
	}
    
    public int getHeads() {
        return this.heads;
    }

	CoinFlip(int flips) {
		this.rand = new Random(); 
		this.tosses = flips;
        this.heads = 0;
	}

	public static void main(String[] args) {
		if(args.length != 2) {
			System.out.println("Usage: CoinFlip #threads #iterations");
			return;
		}

		int numThreads = Integer.parseInt(args[0]);
		long totalTosses = Long.parseLong(args[1]);
		int tossesPerThread = (int) (totalTosses / numThreads);
		int remTosses = tossesPerThread + (int) (totalTosses % numThreads); // remaining tosses
		Thread[] threads = new Thread[numThreads];
		long startTime = System.currentTimeMillis();

		int work = tossesPerThread;
		
        int totalHeads = 0;
        CoinFlip[] coins = new CoinFlip[numThreads];
        
		for(int i = 0; i < numThreads; i++) {
			if(i == numThreads - 1) {
				work = remTosses; // assign all remaining tosses to last thread
			}
            
            coins[i] = new CoinFlip(work);
			threads[i] = new Thread(coins[i]);
			threads[i].start();
		}
		
		for(int j = 0; j < numThreads; j++) {
			try {
				threads[j].join();
			}
			catch(InterruptedException e) {
				System.out.println("Thread interrupted.");
                System.out.println("Exception: " + e.toString());
                System.out.println("Message: " + e.getMessage());
        		return;
			}
        }
        
        for (int j = 0; j < numThreads; j++) {
            totalHeads += coins[j].getHeads();
        }
		
		long timeTaken = System.currentTimeMillis() - startTime;
		System.out.println(totalHeads + " heads in " + totalTosses + " coin tosses");
		System.out.println("Elapsed time: " + timeTaken + "ms");
	}
}
