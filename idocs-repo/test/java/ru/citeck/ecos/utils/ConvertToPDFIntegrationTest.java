package ru.citeck.ecos.utils;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import ru.citeck.ecos.test.ApplicationContextHelper;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.ContentService;

public class ConvertToPDFIntegrationTest {

	private static ApplicationContext context = null;
	private static ContentService contentService;
	private static String rtf;
	
	@BeforeClass
	public static void setUp() throws Exception {
		context = ApplicationContextHelper.getApplicationContext();
		contentService = ((ContentService)context.getBean("contentService"));
		rtf = "application/rtf";
	
	}
	@Test
	public void pngToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_IMAGE_PNG,MimetypeMap.MIMETYPE_PDF));
	}
	
	@Test
	public void gifToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_IMAGE_GIF,MimetypeMap.MIMETYPE_PDF));
	}
	
	@Test
	public void jpegToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_IMAGE_JPEG,MimetypeMap.MIMETYPE_PDF));
	}
	
	@Test
	public void msgToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_OUTLOOK_MSG,MimetypeMap.MIMETYPE_PDF));
	}

	@Test
	public void xlsToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_EXCEL,MimetypeMap.MIMETYPE_PDF));
	}

	@Test
	public void odtToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT,MimetypeMap.MIMETYPE_PDF));
	}

	@Test
	public void pptToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_PPT,MimetypeMap.MIMETYPE_PDF));
	}

	@Test
	public void rtfToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(rtf,MimetypeMap.MIMETYPE_PDF));
	}

	@Test
	public void txtToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_TEXT_PLAIN,MimetypeMap.MIMETYPE_PDF));
	}

	@Test
	public void docToPDFTest() {
		assertEquals(Boolean.TRUE, checkTansformation(MimetypeMap.MIMETYPE_WORD,MimetypeMap.MIMETYPE_PDF));
	}
	
	private boolean checkTansformation(String source, String target)
	{
		return contentService.getTransformer(source,target)!=null;
	}

}