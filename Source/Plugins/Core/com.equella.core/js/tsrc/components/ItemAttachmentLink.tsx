/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { Link } from "@material-ui/core";
import * as React from "react";
import { SyntheticEvent, useState } from "react";
import { ViewerDefinition } from "../modules/ViewerModule";
import Lightbox from "./Lightbox";

export interface ItemAttachmentLinkProps {
  /**
   * Content to be surrounded by the link.
   */
  children: React.ReactNode;
  /**
   * Optional description that will be passed to chosen viewer - e.g. The viewer provided by
   * the `<Lightbox/>` component.
   */
  description?: string;
  /**
   * Optional mimeType that will be passed to chosen viewer - e.g. The viewer provided by
   * the `<Lightbox/>` component.
   */
  mimeType?: string;
  /**
   * Viewer details for the attachment this link is pointing to. Controls which viewer is triggered
   * when the link is clicked.
   */
  viewerDetails: ViewerDefinition;
}

/**
 * A component to be used for viewing attachments in a uniform manner. If the viewer specified
 * in `viewerDetails` is anything other than `lightbox` then a simple link will be created. In
 * future versions of oEQ when the balance of New UI is undertaken, then in theory this component
 * will need to handle the other types of viewers - e.g. generating links for google docs, or
 * downloading attachments, etc.
 */
const ItemAttachmentLink = ({
  children,
  description,
  mimeType,
  viewerDetails: [viewer, url],
}: ItemAttachmentLinkProps) => {
  const [showLightbox, setShowLightbox] = useState<boolean>(false);

  const buildLightboxLink = (): JSX.Element => {
    if (!mimeType) {
      throw new Error(
        "'mimeType' must be specified when viewer is 'lightbox'."
      );
    }

    return (
      <>
        <Link
          component="button"
          onClick={(event: SyntheticEvent) => {
            setShowLightbox(!showLightbox);
            event.stopPropagation();
          }}
        >
          {children}
        </Link>
        {showLightbox && ( // minor optimisation to minimise DOM
          <Lightbox
            mimeType={mimeType}
            onClose={() => setShowLightbox(false)}
            open={showLightbox}
            src={url}
            title={description}
          />
        )}
      </>
    );
  };

  return viewer === "lightbox" ? (
    buildLightboxLink()
  ) : (
    // Lightbox viewer not specified, so go with the default of a simple link.
    <Link href={url} target="_blank" rel="noreferrer">
      {children}
    </Link>
  );
};

export default ItemAttachmentLink;
