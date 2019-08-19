package ru.citeck.ecos.version;

import org.alfresco.model.ContentModel;
import org.alfresco.module.versionsdiff.util.diff_match_patch;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.service.cmr.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedList;

@Component
public class VersionDifferenceUtils {

    private final ContentService contentService;

    @Autowired
    public VersionDifferenceUtils(@Qualifier("ContentService") ContentService contentService) {
        this.contentService = contentService;
    }

    public LinkedList<String[]> getDiff(NodeRef first, NodeRef second) {
        // Instantiate the diff_match_patch object
        diff_match_patch diffMatchPatch = new diff_match_patch();

        // selectedVersRef is the first parameter for INSERT and DELETE right computation
        LinkedList<diff_match_patch.Diff> diffList = diffMatchPatch.diff_main(
                getPlainTxtTrasformation(first),
                getPlainTxtTrasformation(second));

        // semantic cleanup post-processing for human readable differentiation
        diffMatchPatch.diff_cleanupSemantic(diffList);

        LinkedList<String[]> diffObjList = new LinkedList<>();

        // loop through the Diffs LinkedList
        while (!diffList.isEmpty()) {
            // Pop of the first element in the list
            diff_match_patch.Diff element = diffList.pop();
            String[] obj = {element.operation.toString(), element.text};
            diffObjList.add(obj);
        }

        return diffObjList;
    }

    /**
     * Transform the passed document in plain/text string
     *
     * @param nodeRef the node reference of the document to transform in plain/text
     * @return a string with the content of the document or "" if something goes wrong
     */
    private String getPlainTxtTrasformation(NodeRef nodeRef) {
        ContentReader reader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (reader != null && reader.exists()) {
            // we have a transformer that is fast enough
            ContentWriter writer = contentService.getTempWriter();
            writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);

            TransformationOptions options = new TransformationOptions();

            if (contentService.isTransformable(reader, writer, options)) {
                try {
                    contentService.transform(reader, writer, options);
                    // point the reader to the new-written content
                    reader = writer.getReader();
                    // Check that the reader is a view onto something concrete
                    if (!reader.exists()) {
                        throw new ContentIOException("The transformation did not write any content, yet: \n"
                                + "   temp writer:     " + writer);
                    } else {
                        return reader.getContentString();
                    }

                } catch (ContentIOException ignored) {
                }
            }
            return "No trasformer for this type of File";
        }
        return "Content Reader fail";
    }

}
