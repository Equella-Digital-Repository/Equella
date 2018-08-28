module ItemSummary where 

import Prelude

import Data.Argonaut (Json, decodeJson, (.?), (.??))
import Data.Either (Either(..))
import Data.Maybe (Maybe)
import Data.Traversable (traverse)
import Foreign.Object (Object)

data MetaType = Text | HTML | Date 

type MetaDisplay = {
  title :: String,
  value :: String,
  fullWidth :: Boolean,
  metaType :: MetaType
}

data AttachmentNode = Attachment AttachmentView (Array AttachmentNode)

type AttachmentView = {
  title:: String,
  uuid :: String, 
  href :: String,
  thumbnailHref :: String, 
  viewers :: Object String,
  details :: Array MetaDisplay
}

data ItemSummarySection = 
      BasicDetails {title::String, description::Maybe String} 
    | DisplayNodes {sectionTitle::String, meta::Array MetaDisplay}
    | Attachments {sectionTitle::String, attachments::Array AttachmentNode}
    | HtmlSummarySection {sectionTitle::String, html::String}
    | CommentsSummarySection {sectionTitle::String }

type ItemSummary = {
  title :: String,
  sections :: Array ItemSummarySection
}

decodeBasic :: Object Json -> Either String ItemSummarySection
decodeBasic o = do 
  title <- o .? "title"
  description <- o .?? "description"
  pure $ BasicDetails {title,description}

decodeMeta :: Object Json -> Either String MetaDisplay
decodeMeta o = do 
  title <- o .? "title"
  value <- o .? "value"
  fullWidth <- o .? "fullWidth"
  metaType <- o .? "type" >>= case _ of 
    "text" -> pure Text 
    "html" -> pure HTML 
    "date" -> pure Date 
    nd -> Left $ "Unknown display node type: '" <> nd <> "'"
  pure {title,value,fullWidth,metaType}

decodeAttachmentView :: Object Json -> Either String AttachmentView
decodeAttachmentView o = do 
  title <- o .? "title"
  uuid <- o .? "uuid"
  href <- o .? "href"
  thumbnailHref <- o .? "thumbnailHref"
  viewers <- o .? "viewers"
  details <- o .? "details" >>= traverse decodeMeta
  pure {title,uuid,href,thumbnailHref,viewers,details}

decodeAttachment :: Object Json -> Either String AttachmentNode
decodeAttachment o = do 
  view <- decodeAttachmentView o 
  pure $ Attachment view []

decodeAttachments :: Object Json -> Either String ItemSummarySection
decodeAttachments o = do 
  sectionTitle <- o .? "sectionTitle"
  attachments <- o .? "attachments" >>= traverse decodeAttachment
  pure $ Attachments {sectionTitle, attachments}

decodeDisplayNodes :: Object Json -> Either String ItemSummarySection
decodeDisplayNodes o = do 
  sectionTitle <- o .? "sectionTitle"
  meta <- o .? "meta" >>= traverse decodeMeta
  pure $ DisplayNodes {meta,sectionTitle}

decodeHtmlSection :: Object Json -> Either String ItemSummarySection
decodeHtmlSection o = do 
  sectionTitle <- o .? "sectionTitle"
  html <- o .? "html"
  pure $ HtmlSummarySection {sectionTitle,html}

decodeCommentsSection :: Object Json -> Either String ItemSummarySection
decodeCommentsSection o = do 
  sectionTitle <- o .? "sectionTitle"
  pure $ CommentsSummarySection {sectionTitle}

decodeSection :: Json -> Either String ItemSummarySection
decodeSection v = do 
  o <- decodeJson v
  t <- o .? "type" >>= case _ of 
          "basic" -> decodeBasic o 
          "displayNodes" -> decodeDisplayNodes o 
          "attachments" -> decodeAttachments o
          "html" -> decodeHtmlSection o
          "comments" -> decodeCommentsSection o 
          st -> Left $ "Unknown section type '" <> st <> "'"
  pure t

decodeItemSummary :: Json -> Either String ItemSummary 
decodeItemSummary v = do 
  o <- decodeJson v
  title <- o .? "title"
  sections <- o .? "sections" >>= traverse decodeSection
  pure {title, sections}
