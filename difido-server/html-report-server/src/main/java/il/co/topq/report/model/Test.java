package il.co.topq.report.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Test {

	private String name;
	private String clazz;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	@Override
	public String toString() {
		return "Name: " + name + ", clazz" + clazz;
	}

}
