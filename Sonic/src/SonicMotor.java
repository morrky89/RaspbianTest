import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.wiringpi.SoftPwm;

public class SonicMotor {

	final static GpioController gpio = GpioFactory.getInstance();

	final static GpioPinDigitalOutput m1_1 = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_07, "m11", PinState.LOW);
	final static GpioPinDigitalOutput m1_2 = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_00, "m12", PinState.LOW);
	
	final static GpioPinDigitalOutput m1_e = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_01, "m1e", PinState.LOW);
	
	final static GpioPinDigitalOutput s_trigger = gpio.provisionDigitalOutputPin(
			RaspiPin.GPIO_15, "trigger", PinState.LOW);
	final static GpioPinDigitalInput s_echo = gpio.provisionDigitalInputPin(
			RaspiPin.GPIO_16, "echo");
	
	
	static void clockwise() {
		m1_1.high();
		m1_2.low();
	}
	
	static void counter_clockwise() {
		m1_1.low();
		m1_2.high();
	}
	
	static double measure()  {
		try {
		s_trigger.high();
		Thread.sleep(0,1000);
		s_trigger.low();
		}catch (InterruptedException e) {
		e.printStackTrace();
		System.out.println("failed");
		}
//		s_trigger.low();
		double start, stop;
		start = System.nanoTime();
		stop = System.nanoTime();
		
		while (s_echo.getState() == PinState.LOW){
				start = System.nanoTime();
		if(start-stop > 10000000.0) 
			break;

		//System.out.println("while low " + (start-stop));
		}
		
		while(s_echo.getState() == PinState.HIGH) {
				stop = System.nanoTime();
		//System.out.println("while high");
		}
		
		if((stop-start)>= 50000 && (stop-start)<= 10000000) {
		double elapsed = stop-start;
		double seconds = elapsed*0.000000001;
//		System.out.println(elapsed);
		return (seconds*34300)/2;
		}else {
//		System.out.println("Time out");
		return 0;
		}	
	}
	
	static double measure_average()  {
		double aver=0;
		try {

		Thread.sleep(60,0);
		double one = measure();
		if(one ==0)
			one=measure();
//		System.out.println(one);
		Thread.sleep(60,0);
		double two = measure();
		if(two == 0)
			measure();

		Thread.sleep(60,0);
		double three = measure();
		if(three == 0)
			three = measure();


//		System.out.println(one + "  " + two + "   "+ three);

		
		
		aver = (one+ three+two)/3;
//		System.out.println("average: " + aver);
		}catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("failed");
                }

		return aver;
	}
	
	
	
	static void clear() {
		gpio.setState(PinState.LOW, m1_1,m1_2,s_trigger);
		m1_e.high();
	}

	public static void main(String[] args) throws InterruptedException {

		
		SoftPwm.softPwmCreate(1, 10, 100);
		//clockwise();
		
		
		System.out.println("___DC motor and ultrasonic sensor___");
		clear();

		boolean running = true;
		//Thread.sleep(2000);
		double d = 0;
		double p = 0;
		double diff = 0;
		while (running) {
			//Thread.sleep(1,0);
			d = measure_average();
			//System.out.println((int)(90));
			if(d<=11 && d>=9 ){
			m1_1.low();
                        m1_2.low();
			} else {
			if ( d<9 ) {
			clockwise();
			}else {
			counter_clockwise();
			}
			}
			diff = Math.abs(p-d);
			if(diff>2 && diff < 20) {
			SoftPwm.softPwmWrite(1,95);
			}else {if(diff > 20) {
			SoftPwm.softPwmWrite(1,100);
			}else {
			SoftPwm.softPwmWrite(1,92);
			}}
//			System.out.println(Math.abs(p-d));
			p = d;
		}
		//}catch (InterruptedException e) {
		//System.out.println("Fail");
		//}
		clear();
		gpio.shutdown();
		
	}

}
