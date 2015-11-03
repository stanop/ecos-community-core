/*
 * Copyright (C) 2008-2015 Citeck LLC.
 *
 * This file is part of Citeck EcoS
 *
 * Citeck EcoS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Citeck EcoS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Citeck EcoS. If not, see <http://www.gnu.org/licenses/>.
 */
package ru.citeck.ecos.webscripts.security;

import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import com.objsys.asn1j.runtime.Asn1ObjectIdentifier;
import com.objsys.asn1j.runtime.Asn1OctetString;
import com.objsys.asn1j.runtime.Asn1Type;

import java.io.ByteArrayInputStream;
import java.security.Security;
import java.security.MessageDigest;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerInfo;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Attribute;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCP.tools.Decoder;

public class SignatureUtil {
	
	private static volatile SignatureUtil instance;
	private static Log _log = LogFactory.getLog(SignatureUtil.class);
	
	
	/**
	 * Constants
	 */
	public static final String STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2";
	public static final String STR_CMS_OID_CONT_TYP_ATTR = "1.2.840.113549.1.9.3";
	public static final String STR_CMS_OID_DIGEST_ATTR = "1.2.840.113549.1.9.4";
	public static final String STR_CMS_OID_SIGN_TYM_ATTR = "1.2.840.113549.1.9.5";
	public static final String DIGEST_OID = JCP.GOST_DIGEST_OID;
	public static final String DIGEST_ALG_NAME = JCP.GOST_DIGEST_NAME;
	
	
	/**
	 * Constructor 
	 */
	private SignatureUtil() {
		if (Security.getProvider("JCP") == null) { 
			Security.addProvider(new ru.CryptoPro.JCP.JCP());
		}
	}
	
	
	/**
	 * Singleton controller
	 * 
	 * @return instance of class
	 */
	public static SignatureUtil getInstance() {
		SignatureUtil localInstance = instance;
		
		if (localInstance == null) {
            synchronized (SignatureUtil.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new SignatureUtil();
                }
            }
        }
        return localInstance;
    }
	
	
	/**
	 * Verifies signature with certificate included
	 * 
	 * @param signature CryptoPro signed message (signature) with or without data
	 * @param data Data to sign if they are not included in signed message (binary or UTF16-LE)
	 * @return true if signature valid, false otherwise
	 * @throws Exception 
	 */
	public List<String> verifySignature(byte[] signatureBase64, byte[] data) throws Exception {
		int validSigns = 0;
		List<String> validCertOwners = new ArrayList<String>();
		
		// decode signature from Base64 to der
		final Decoder decoder = new Decoder();
	    final byte[] signature = decoder.decodeBuffer(new ByteArrayInputStream(signatureBase64));
	    
		// create internal object (cms) for signature
		final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(signature);
		final ContentInfo all = new ContentInfo();
		all.decode(asnBuf);

		if (!new OID(STR_CMS_OID_SIGNED).eq(all.contentType.value))
			throw new Exception("Signature type not supported");

		final SignedData cms = (SignedData) all.content;

		// create internal object for data to sign
		final byte[] text;
		if (cms.encapContentInfo.eContent != null)
			text = cms.encapContentInfo.eContent.value;
		else if (data != null)
			text = data;
		else
			throw new Exception("No content for verify");

		// create and check digest
		OID digestOid = null;
		final DigestAlgorithmIdentifier digestAlgorithmIdentifier = new DigestAlgorithmIdentifier(
				new OID(DIGEST_OID).value);
		for (int i = 0; i < cms.digestAlgorithms.elements.length; i++) {
			if (cms.digestAlgorithms.elements[i].algorithm.equals(digestAlgorithmIdentifier.algorithm)) {
				digestOid = new OID(cms.digestAlgorithms.elements[i].algorithm.value);
				break;
			}
		}
		if (digestOid == null) throw new Exception("Unknown digest");
		final OID eContTypeOID = new OID(cms.encapContentInfo.eContentType.value);

		if (cms.certificates != null) {
			// verification using included certificates
			for (int i = 0; i < cms.certificates.elements.length; i++) {
				final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
				cms.certificates.elements[i].encode(encBuf);

				final CertificateFactory cf = CertificateFactory.getInstance("X.509");
				final X509Certificate cert = (X509Certificate) cf.generateCertificate(encBuf.getInputStream());

				for (int j = 0; j < cms.signerInfos.elements.length; j++) {
					final SignerInfo info = cms.signerInfos.elements[j];
					if (!digestOid.equals(new OID(info.digestAlgorithm.algorithm.value)))
						throw new Exception("Not signed on certificate.");
					final boolean checkResult = verifyOnCert(cert, 
						cms.signerInfos.elements[j], text, eContTypeOID);
					if (checkResult) {
						validSigns++;
						String[] certNameParts = cert.getSubjectX500Principal().getName().split(",");
						String CN = "Unknown Signer";
						String O = null;
						String fingerPrint = null;
						
						for (int k = 0; k < certNameParts.length; k++) {
							String[] parts = certNameParts[k].split("="); 
							if (parts.length == 2) {
								if (parts[0].equals("CN")) CN = parts[1];
								if (parts[0].equals("O")) O = parts[1];
								if (parts[0].equals("1.2.840.113549.1.9.1")) fingerPrint = parts[1];
							}
						}
						
						StringBuilder sb = new StringBuilder(CN);
						if (O != null) sb.append(", ").append(O);
						if (fingerPrint != null) sb.append(", ").append(fingerPrint);
						
						validCertOwners.add(sb.toString());
					}	
					
					_log.info("Signature validation result: " + checkResult + ", cert: " + 
							cert.getSubjectX500Principal());
				}
			}
		}

		if ((validSigns == 0) || (validSigns < cms.signerInfos.elements.length))
			throw new Exception("signature verification failed: count of valid signatures less than signers count or zero");
		
		return validCertOwners;
	}
	
	
	/**
	 * Check signature using given certificate
	 * 
	 * @param cert Certificate
	 * @param text Data to sign 
	 * @param info Signature
	 * @return true if signature valid, false otherwise
	 * @throws Exception
	 */
	protected boolean verifyOnCert(X509Certificate cert,
			SignerInfo info, byte[] text, OID eContentTypeOID) throws Exception {
		
		// signature
		final byte[] sign = info.signature.value;
		
		// data to sign (get from input or from signed message)
		final byte[] data;
		
		if (info.signedAttrs == null) {
			// signature message info without attributes
			data = text;
		} else {
			// signature with attributes (SignedAttr)
			final Attribute[] signAttrElem = info.signedAttrs.elements;

			// check of content-type attribute
			final Asn1ObjectIdentifier contentTypeOid = new Asn1ObjectIdentifier(
					(new OID(STR_CMS_OID_CONT_TYP_ATTR)).value);
			Attribute contentTypeAttr = null;

			for (int r = 0; r < signAttrElem.length; r++) {
				final Asn1ObjectIdentifier oid = signAttrElem[r].type;
				if (oid.equals(contentTypeOid)) {
					contentTypeAttr = signAttrElem[r];
				}
			}

			if (contentTypeAttr == null)
				throw new Exception("content-type attribute not present");

			//if (!contentTypeAttr.values.elements[0].equals(eContentTypeOIDIdentifier))
			//	throw new Exception("content-type attribute OID not equal eContentType OID");

			// check message-digest attribute
			final Asn1ObjectIdentifier messageDigestOid = new Asn1ObjectIdentifier(
					(new OID(STR_CMS_OID_DIGEST_ATTR)).value);

			Attribute messageDigestAttr = null;

			for (int r = 0; r < signAttrElem.length; r++) {
				final Asn1ObjectIdentifier oid = signAttrElem[r].type;
				if (oid.equals(messageDigestOid)) {
					messageDigestAttr = signAttrElem[r];
				}
			}

			if (messageDigestAttr == null)
				throw new Exception("message-digest attribute not present");

			final Asn1Type open = messageDigestAttr.values.elements[0];
			final Asn1OctetString hash = (Asn1OctetString) open;
			final byte[] mdBig = hash.value;
			
			// remove first 2 bytes - they are OctetString type identity (04h) and Length (20h)
			final byte[] md = new byte[32];
			System.arraycopy(mdBig, 2, md, 0, 32);
			
			final byte[] dm = calculateDigest(text, DIGEST_ALG_NAME);

			if (!Array.toHexString(dm).equals(Array.toHexString(md)))
				throw new Exception("message-digest attribute verify failed");

			// check signing-time attribute
			final Asn1ObjectIdentifier signTimeOid = new Asn1ObjectIdentifier(
					(new OID(STR_CMS_OID_SIGN_TYM_ATTR)).value);

			Attribute signTimeAttr = null;

			for (int r = 0; r < signAttrElem.length; r++) {
				final Asn1ObjectIdentifier oid = signAttrElem[r].type;
				if (oid.equals(signTimeOid)) {
					signTimeAttr = signAttrElem[r];
				}
			}

			if (signTimeAttr != null) {
				// check attr if needed
			}

			// and finally extract data to verify (need to check CMS-part, not source data)
			final Asn1BerEncodeBuffer encBufSignedAttr = new Asn1BerEncodeBuffer();
			info.signedAttrs.encode(encBufSignedAttr);
			data = encBufSignedAttr.getMsgCopy();
		}
		
		// verification
		final Signature signature = Signature.getInstance(JCP.GOST_EL_SIGN_NAME);
		signature.initVerify(cert);
		signature.update(data);
		
		return signature.verify(sign);
	}

	
	/**
	 * Calculate message digest
	 *  
	 * @param message Data
	 * @param digestAlgorithmName AlgorithmName
	 * @return Message digest
	 * @throws Exception
	 */
	public byte[] calculateDigest(byte[] message, String digestAlgorithmName) throws Exception {
	    final MessageDigest digest = MessageDigest.getInstance(digestAlgorithmName);
	    digest.update(message);
	    return digest.digest();
	}

}
