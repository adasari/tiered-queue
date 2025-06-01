package org.tq;

public class Main {
    public static void main(String[] args) throws Exception {
        TieredQueue tieredQueue = new TieredQueue("data", 100);

        // Writer thread
        new Thread(() -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    tieredQueue.write("event-" + i);
                    Thread.sleep(2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // Reader thread
        new Thread(() -> {
            while (true) {
                String msg = tieredQueue.read();
                if (msg != null) {
                    System.out.println("READ: " + msg);
                }
                try { Thread.sleep(5); } catch (InterruptedException ignored) {}
            }
        }).start();
    }
}

