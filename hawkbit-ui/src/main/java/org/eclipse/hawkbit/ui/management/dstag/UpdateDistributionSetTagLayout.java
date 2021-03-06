/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.ui.management.dstag;

import java.util.Optional;

import org.eclipse.hawkbit.repository.DistributionSetTagManagement;
import org.eclipse.hawkbit.repository.EntityFactory;
import org.eclipse.hawkbit.repository.builder.TagUpdate;
import org.eclipse.hawkbit.repository.exception.EntityNotFoundException;
import org.eclipse.hawkbit.repository.model.DistributionSetTag;
import org.eclipse.hawkbit.repository.model.Tag;
import org.eclipse.hawkbit.ui.SpPermissionChecker;
import org.eclipse.hawkbit.ui.colorpicker.ColorPickerConstants;
import org.eclipse.hawkbit.ui.colorpicker.ColorPickerHelper;
import org.eclipse.hawkbit.ui.common.table.BaseEntityEventType;
import org.eclipse.hawkbit.ui.layouts.UpdateTag;
import org.eclipse.hawkbit.ui.management.event.DistributionSetTagTableEvent;
import org.eclipse.hawkbit.ui.utils.UINotification;
import org.eclipse.hawkbit.ui.utils.VaadinMessageSource;
import org.vaadin.spring.events.EventBus.UIEventBus;

import com.vaadin.ui.Window.CloseListener;

/**
 * Layout for the pop-up window which is created when updating a distribution
 * set tag on the Deployment View.
 *
 */
public class UpdateDistributionSetTagLayout extends AbstractDistributionSetTagLayout implements UpdateTag {

    private static final long serialVersionUID = 1L;

    private final String selectedTagName;

    private final CloseListener closeListener;

    /**
     * Constructor
     * 
     * @param i18n
     *            VaadinMessageSource
     * @param distributionSetTagManagement
     *            DistributionSetTagManagement
     * @param entityFactory
     *            EntityFactory
     * @param eventBus
     *            UIEventBus
     * @param permChecker
     *            SpPermissionChecker
     * @param uiNotification
     *            UINotification
     * @param selectedTagName
     *            name of the selected distribution set tag to update
     * @param closeListener
     *            CloseListener
     */
    public UpdateDistributionSetTagLayout(final VaadinMessageSource i18n,
            final DistributionSetTagManagement distributionSetTagManagement, final EntityFactory entityFactory,
            final UIEventBus eventBus, final SpPermissionChecker permChecker, final UINotification uiNotification,
            final String selectedTagName, final CloseListener closeListener) {
        super(i18n, entityFactory, eventBus, permChecker, uiNotification, distributionSetTagManagement);
        this.selectedTagName = selectedTagName;
        this.closeListener = closeListener;
        initUpdatePopup();
    }

    private void initUpdatePopup() {
        setTagDetails(selectedTagName);
        getWindow().addCloseListener(closeListener);
    }

    @Override
    protected String getWindowCaption() {
        return getI18n().getMessage("caption.update", getI18n().getMessage("caption.tag"));
    }

    @Override
    protected void saveEntity() {
        updateExistingTag(findEntityByName()
                .orElseThrow(() -> new EntityNotFoundException(DistributionSetTag.class, getTagName().getValue())));
    }

    private void updateExistingTag(final Tag targetObj) {
        final TagUpdate update = getEntityFactory().tag().update(targetObj.getId()).name(getTagName().getValue())
                .description(getTagDesc().getValue())
                .colour(ColorPickerHelper.getColorPickedString(getColorPickerLayout().getSelPreview()));
        getDistributionSetTagManagement().update(update);
        getEventBus().publish(this,
                new DistributionSetTagTableEvent(BaseEntityEventType.UPDATED_ENTITY, (DistributionSetTag) targetObj));
        getUiNotification().displaySuccess(getI18n().getMessage("message.update.success", targetObj.getName()));
    }

    @Override
    public void setTagDetails(final String selectedEntity) {
        final Optional<DistributionSetTag> selectedDistTag = getDistributionSetTagManagement()
                .getByName(selectedEntity);
        selectedDistTag.ifPresent(tag -> {
            getTagName().setValue(tag.getName());
            getTagName().setEnabled(false);
            getTagDesc().setValue(selectedDistTag.get().getDescription());
            if (selectedDistTag.get().getColour() == null) {
                setTagColor(getColorPickerLayout().getDefaultColor(), ColorPickerConstants.DEFAULT_COLOR);
            } else {
                setTagColor(ColorPickerHelper.rgbToColorConverter(selectedDistTag.get().getColour()),
                        selectedDistTag.get().getColour());
            }
            getWindow().setOrginaleValues();
        });
    }

    @Override
    protected boolean isUpdateAction() {
        return true;
    }

}
