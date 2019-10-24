from google.cloud import bigquery_storage_v1beta1


def get_table_ref():
    table_ref = bigquery_storage_v1beta1.types.TableReference()
    table_ref.project_id = "ml-sandbox-1-191918"
    table_ref.dataset_id = "blackfriday"
    table_ref.table_id = "user_summaries"
    return table_ref


def get_read_options(partition_name):
    read_options = bigquery_storage_v1beta1.types.TableReadOptions()
    read_options.selected_fields.append("Purchase_Total")
    read_options.selected_fields.append("Purchase_Count")
    for x in range(21):
        if (x < 10):
            read_options.selected_fields.append("Occupation_%02d" % (x))
        else: 
            read_options.selected_fields.append("Occupation_%d" % (x))
    read_options.selected_fields.append("City_Category_a")
    read_options.selected_fields.append("City_Category_b")
    read_options.selected_fields.append("City_Category_c")
    for x in range(5):
        read_options.selected_fields.append(
            "Stay_In_Current_City_Years_%d" % (x))
    read_options.selected_fields.append("Gender_m")
    read_options.selected_fields.append("Gender_f")
    read_options.selected_fields.append("Age_0_17")
    read_options.selected_fields.append("Age_18_25")
    read_options.selected_fields.append("Age_26_35")
    read_options.selected_fields.append("Age_36_45")
    read_options.selected_fields.append("Age_46_50")
    read_options.selected_fields.append("Age_51_55")
    read_options.selected_fields.append("Age_55")
    for x in range(1, 21):
        if (x < 10):
            read_options.selected_fields.append("Product_Category_1_%02d" % (x))
        else: 
            read_options.selected_fields.append("Product_Category_1_%d" % (x))
    for x in range(1, 19):
        if (x < 10):
            read_options.selected_fields.append("Product_Category_2_%02d" % (x))
        else: 
            read_options.selected_fields.append("Product_Category_2_%d" % (x))
    for x in range(1, 19):
        if (x < 10):
            read_options.selected_fields.append("Product_Category_3_%02d" % (x))
        else: 
            read_options.selected_fields.append("Product_Category_3_%d" % (x))

    # These vals don't exist
    read_options.selected_fields.remove("Product_Category_2_01")
    read_options.selected_fields.remove("Product_Category_3_07")
    read_options.selected_fields.remove("Product_Category_3_01")
    read_options.selected_fields.remove("Product_Category_3_02")

    read_options.selected_fields.append

    read_options.row_restriction = 'ml_partition = "{}"'.format(partition_name)
    return read_options


def get_session(client, table_ref, read_options, parent):
    return client.create_read_session(
        table_ref,
        parent,
        table_modifiers=None,
        read_options=read_options,
        # This API can also deliver data serialized in Apache Arrow format.
        # This example leverages Apache Avro.
        format_=bigquery_storage_v1beta1.enums.DataFormat.AVRO,
        # We use a LIQUID strategy in this example because we only read from a
        # single stream. Consider BALANCED if you're consuming multiple streams
        # concurrently and want more consistent stream sizes.
        sharding_strategy=(bigquery_storage_v1beta1.enums.ShardingStrategy.LIQUID),
    )


def get_reader(client, session):
    return client.read_rows(bigquery_storage_v1beta1.types.StreamPosition(stream=session.streams[0]))


def get_df(reader, session):
    rows = reader.rows(session)
    return rows.to_dataframe()


def get_data_partition(partition_name):
    client = bigquery_storage_v1beta1.BigQueryStorageClient()
    session = get_session(client, get_table_ref(), get_read_options(partition_name), "projects/{}".format(get_table_ref().project_id))
    reader = get_reader(client, session)
    return get_df(reader, session)
