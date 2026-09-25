package com.cec.helpdesk.adapter;

/**
 * DESIGN PATTERN — ADAPTER (generic interface)
 *
 * Converts a domain model object into a display-friendly string array
 * suitable for JTable rows.  Each concrete adapter adapts a different
 * model class to the same tabular output format expected by the View layer.
 *
 * @param <T> the source model type to adapt
 */
public interface DataAdapter<T> {

    /**
     * Returns the column headers for the adapted data.
     */
    String[] getColumnNames();

    /**
     * Converts a single model object into a row of display strings.
     */
    Object[] toRowData(T item);

    /**
     * Adapts a model object into an HTML card/row representation
     * conforming to the Warm Parchment Atelier design system.
     */
    String toHtml(T item, com.cec.helpdesk.model.User currentUser);
}
