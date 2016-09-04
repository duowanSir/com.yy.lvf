package com.example.interrupted;

public class MyThreadInterrupted {

	public static void main(String[] args) {
		Thread t = new Thread() {
			@Override
			public void run() {
				super.run();
				try {
					System.out.println(this.toString() + " is interrupted " + isInterrupted());
					sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
					System.out.println(this.toString() + " is interrupted " + isInterrupted());
					Thread.interrupted();
					System.out.println(this.toString() + " is interrupted " + isInterrupted());
				}

			}
		};
		t.start();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t.interrupt();

		Thread t1 = new Thread() {
			@Override
			public void run() {
				super.run();
				while (!isInterrupted()) {
					System.out.println(this.toString() + " running fast");
				}
				System.out.println(this.toString() + " is interrupted " + isInterrupted());
				System.out.println(this.toString() + " interrupted " + interrupted());
				System.out.println(this.toString() + " interrupted " + interrupted());
			}
		};
		t1.start();
		try {
			Thread.sleep(3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		t1.interrupt();
	}

}
