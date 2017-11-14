package life.genny;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.lang.invoke.MethodHandles;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeBoolean;
import life.genny.qwanda.attribute.AttributeDate;
import life.genny.qwanda.attribute.AttributeDateTime;
import life.genny.qwanda.attribute.AttributeDouble;
import life.genny.qwanda.attribute.AttributeLong;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.entity.Person;
import life.genny.qwanda.entity.Product;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwandautils.ScoringUtils;

public class ScoringTest {
	
	  private static final Logger log = org.apache.logging.log4j.LogManager
		      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	Person person;
	Product product;
	
	@Before
	public void setup() throws BadDataException {
		
	//	AttributeText attributeText1 = new AttributeText(AttributeText.getDefaultCodePrefix()+"TEST1","Test 1");
	//	AttributeText attributeText2 = new AttributeText(AttributeText.getDefaultCodePrefix()+"TEST2","Test 2");
		AttributeText attributeText3 = new AttributeText(AttributeText.getDefaultCodePrefix()+"TEST3","Test 3");
		
	//	Attribute attributeDouble = new AttributeDouble(AttributeDouble.getDefaultCodePrefix()+"TEST4","Test Double 4");
	//	Attribute attributeLong = new AttributeLong(AttributeLong.getDefaultCodePrefix()+"TEST5","Test Long 5");	
		Attribute attributeBoolean = new AttributeBoolean(AttributeBoolean.getDefaultCodePrefix()+"TEST6","Test Boolean 6");	
		Attribute attributeDateTime = new AttributeDateTime(AttributeDateTime.getDefaultCodePrefix()+"TEST7","Test DateTiume 7");	
		Attribute attributeDate = new AttributeDate(AttributeDate.getDefaultCodePrefix()+"TEST8","Test Date 8");	
		
		
		person = new Person("Barry Allen");
		
//		person.addAttribute(attributeText1, 1.0);
//		person.addAttribute(attributeText2, 0.8);
		person.addAttribute(attributeText3, 0.9, "3147");
//		person.addAttribute(attributeDouble, 0.6, 3.141);
//		person.addAttribute(attributeLong, 0.6, 3147L);
		person.addAttribute(attributeBoolean, 0.6, true);
		person.addAttribute(attributeDateTime, 0.3, LocalDateTime.of(2017, Month.JUNE, 20, 10, 13));
		person.addAttribute(attributeDate, 0.5, LocalDate.of(2017, Month.JUNE, 20));

		
		product = new Product(Product.getDefaultCodePrefix()+"TEST_PRODUCT","Test Product");
		
//		product.addAttribute(attributeText1, 1.0,"Test 1A");
//		product.addAttribute(attributeText2, 0.8, "Test 2A");
		product.addAttribute(attributeText3, 0.8, "3147");
		
		
		Double score = ScoringUtils.calculateScore(person, product);
		// Should match on only attributeText2
		//  => score should be 0.9 * 0.8 = 0.72
		log.info("Score (postcode) = "+score);
		assertThat(score, equalTo(0.72));
		
		product.addAttribute(attributeBoolean,0.7,true); // should now add 0.6*0.7 = 0.42 => total score = 1.14
		score  = ScoringUtils.calculateScore(person, product);
		log.info("Score (+boolean) = "+score);
		assertThat(score, equalTo(1.14));
	
		product.addAttribute(attributeDate, 0.4, LocalDate.of(2017, Month.JUNE, 20)); // should now add 0.5 * 0.4 = 0.20 => total score = 1.34
		score  = ScoringUtils.calculateScore(person, product);
		log.info("Score (+localdate) = "+score);
		assertThat(score, equalTo(1.34));

		product.addAttribute(attributeDateTime, 0.3, LocalDateTime.of(2017, Month.JUNE, 20, 10, 12));
		score  = ScoringUtils.calculateScore(person, product);
		log.info("Score (+localdatetime, mismatch) = "+score);
		assertThat(score, equalTo(1.34));

		product.addAttribute(attributeDateTime, 0.3, LocalDateTime.of(2017, Month.JUNE, 20, 10, 13));
		score  = ScoringUtils.calculateScore(person, product);
		log.info("Score (+localdatetime) = "+score);
		assertThat(score, equalTo(1.43));

	}

	@After
	public void tearDown() {
		person = null;
		product = null;
	}
	
	@Test
	public void scoringTest()
	{
		
		
		
	}
}
